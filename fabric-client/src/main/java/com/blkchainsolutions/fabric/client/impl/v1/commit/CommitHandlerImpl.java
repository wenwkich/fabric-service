package com.blkchainsolutions.fabric.client.impl.v1.commit;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import org.hyperledger.fabric.gateway.ContractException;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.impl.GatewayUtils;
import org.hyperledger.fabric.gateway.impl.commit.CommitStrategy;
import org.hyperledger.fabric.gateway.spi.CommitHandler;
import org.hyperledger.fabric.gateway.spi.CommitListener;
import org.hyperledger.fabric.gateway.spi.PeerDisconnectEvent;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.BlockListener;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.BlockEvent.TransactionEvent;
import org.hyperledger.fabric.sdk.exception.TransactionEventException;

public final class CommitHandlerImpl implements CommitHandler {
    private final String transactionId;
    private final Channel channel;
    private final CountDownLatch latch = new CountDownLatch(1);
    private final AtomicReference<ContractException> error = new AtomicReference<>();
    private final BlockListener blockListener;
    private String handle = "";

    public CommitHandlerImpl(final String transactionId, final Channel channel) {
        this.transactionId = transactionId;
        this.channel = channel;
        this.blockListener = event -> 
          event.getEnvelopeInfos().forEach(info -> {
            // if transaction id not arrived, keep listening
            if (!transactionId.equals(info.getTransactionID()) || !channel.getPeers().contains(event.getPeer())) return;

            // if one event is already valid, the cancel listening
            if (info.isValid()) {
              cancelListening();
            // if one transaction is rejected by a peer, than count as fail
            } else {
              String peerName = event.getPeer().getName();
              if (info instanceof TransactionEvent) {
                TransactionEventException cause = new TransactionEventException("Transaction event is invalid", (TransactionEvent) info);
                fail(new ContractException("Transaction commit was rejected by peer " + peerName, cause));
              }
              fail(new ContractException("Transaction failed for unknown reason"));
            }
          });
    }

    @Override
    public void startListening() {
        if (channel.getPeers().isEmpty()) {
            cancelListening();
        } else {
          try { 
            handle = channel.registerBlockListener(blockListener);
          } catch (Exception e) {
            error.set(new ContractException("Unable to register block listener to the channel", e));
            cancelListening();
          }
        }
    }

    @Override
    public void waitForEvents(final long timeout, final TimeUnit timeUnit) throws ContractException, TimeoutException, InterruptedException {
        try {
            boolean complete = latch.await(timeout, timeUnit);
            if (!complete) {
                throw new TimeoutException("Timeout waiting for commit of transaction " + transactionId);
            }
        } finally {
            cancelListening();
        }

        ContractException cause = error.get();
        if (cause != null) {
            throw cause;
        }
    }

    @Override
    public void cancelListening() {
      try {
        channel.unregisterBlockListener(handle);
      } catch (Exception e) {
        // supress the warning
      }
      latch.countDown();
    }

    private void fail(final ContractException e) {
        error.set(e);
        cancelListening();
    }
}
