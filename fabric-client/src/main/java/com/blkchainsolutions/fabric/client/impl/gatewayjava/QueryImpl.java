package com.blkchainsolutions.fabric.client.impl.gatewayjava;

import java.nio.charset.StandardCharsets;

import com.blkchainsolutions.fabric.client.exceptions.QueryException;
import com.blkchainsolutions.fabric.client.params.QueryInvokeArgs;
import com.blkchainsolutions.fabric.client.params.QueryInvokeContext;

import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.Transaction;
import org.hyperledger.fabric.gateway.Wallet;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class QueryImpl {

  private final Gateway gateway;
  private final Wallet wallet;
  private final String user;

  public String query(final QueryInvokeArgs args) throws QueryException {
    final QueryInvokeContext context = args.getContext();
    try {
            // Obtain a smart contract deployed on the network.
      Network network = gateway.getNetwork(context.getChannelName());
      Contract contract = network.getContract(context.getContractName());

      // Submit transactions that store state to the ledger.
      Transaction tx = contract.createTransaction(args.getFunctionName());
      TransactionWrapper txWrapper = new TransactionWrapper(tx);
      byte[] result = txWrapper.evaluate(wallet, user, args.getArguments());
      return new String(result, StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new QueryException(e);
    }
  }

}
