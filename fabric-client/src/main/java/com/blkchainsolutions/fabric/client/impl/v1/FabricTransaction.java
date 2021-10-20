package com.blkchainsolutions.fabric.client.impl.v1;

import static org.hyperledger.fabric.sdk.Channel.DiscoveryOptions.createDiscoveryOptions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.blkchainsolutions.fabric.client.impl.v1.commit.CommitHandlerFactory;

import org.hyperledger.fabric.gateway.ContractException;
import org.hyperledger.fabric.gateway.GatewayRuntimeException;
import org.hyperledger.fabric.gateway.impl.TimePeriod;
import org.hyperledger.fabric.gateway.impl.query.QueryImpl;
import org.hyperledger.fabric.gateway.spi.CommitHandler;
import org.hyperledger.fabric.gateway.spi.Query;
import org.hyperledger.fabric.gateway.spi.QueryHandler;
import org.hyperledger.fabric.sdk.ChaincodeResponse;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.Peer.PeerRole;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.QueryByChaincodeRequest;
import org.hyperledger.fabric.sdk.ServiceDiscovery;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
import org.hyperledger.fabric.sdk.TransactionRequest;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.ServiceDiscoveryException;
import org.hyperledger.fabric.sdk.transaction.TransactionContext;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Builder
final class FabricTransaction {

    private static final long DEFAULT_ORDERER_TIMEOUT = 60;
    private static final TimeUnit DEFAULT_ORDERER_TIMEOUT_UNIT = TimeUnit.SECONDS;

    @Getter private final String name;
    @NonNull private final HFClient client;
    @NonNull private final Channel channel;
    private final String contractName;
    private final CommitHandlerFactory commitHandlerFactory;
    private final TimePeriod commitTimeout;
    private final QueryHandler queryHandler;
    private final Map<String, byte[]> transientData;
    private final TransactionContext transactionContext;
    private final boolean discoveryEnabled;

    public String getTransactionId() {
        return transactionContext.getTxID();
    }

    public byte[] submit(final User userContext, final String... args) throws ContractException, TimeoutException, InterruptedException {
        Collection<ProposalResponse> proposalResponses = endorseTransaction(userContext, args);
        Collection<ProposalResponse> validResponses = validatePeerResponses(proposalResponses);

        try {
            return commitTransaction(validResponses);
        } catch (ContractException e) {
            e.setProposalResponses(proposalResponses);
            throw e;
        }
    }

    private Collection<ProposalResponse> endorseTransaction(final User userContext, final String... args) {
        try {
            TransactionProposalRequest request = newProposalRequest(userContext, args);
            return sendTransactionProposal(request);
        } catch (InvalidArgumentException | ProposalException | ServiceDiscoveryException e) {
            throw new GatewayRuntimeException(e);
        }
    }

    private Collection<ProposalResponse> sendTransactionProposal(final TransactionProposalRequest request)
            throws ProposalException, InvalidArgumentException, ServiceDiscoveryException {
        Collection<Peer> endorsingPeers = channel.getPeers(EnumSet.of(PeerRole.ENDORSING_PEER));
        if (endorsingPeers != null) {
            return channel.sendTransactionProposal(request, endorsingPeers);
        } else if (discoveryEnabled) {
            Channel.DiscoveryOptions discoveryOptions = createDiscoveryOptions()
                    .setEndorsementSelector(ServiceDiscovery.EndorsementSelector.ENDORSEMENT_SELECTION_RANDOM)
                    .setInspectResults(true);
            return channel.sendTransactionProposalToEndorsers(request, discoveryOptions);
        } else {
            return channel.sendTransactionProposal(request);
        }
    }

    private byte[] commitTransaction(final Collection<ProposalResponse> validResponses)
            throws TimeoutException, ContractException, InterruptedException {
        ProposalResponse proposalResponse = validResponses.iterator().next();

        CommitHandler commitHandler = commitHandlerFactory.create(getTransactionId(), channel);
        commitHandler.startListening();

        try {
            Channel.TransactionOptions transactionOptions = Channel.TransactionOptions.createTransactionOptions()
                    .nOfEvents(Channel.NOfEvents.createNoEvents()); // Disable default commit wait behaviour
            channel.sendTransaction(validResponses, transactionOptions)
                    .get(DEFAULT_ORDERER_TIMEOUT, DEFAULT_ORDERER_TIMEOUT_UNIT);
        } catch (TimeoutException e) {
            commitHandler.cancelListening();
            throw e;
        } catch (Exception e) {
            commitHandler.cancelListening();
            throw new ContractException("Failed to send transaction to the orderer", e);
        }

        commitHandler.waitForEvents(commitTimeout.getTime(), commitTimeout.getTimeUnit());

        try {
            return proposalResponse.getChaincodeActionResponsePayload();
        } catch (InvalidArgumentException e) {
            throw new GatewayRuntimeException(e);
        }
    }

    private TransactionProposalRequest newProposalRequest(final User userContext, final String... args) {
        TransactionProposalRequest request = client.newTransactionProposalRequest();
        configureRequest(userContext, request, args);
        if (transientData != null) {
            try {
                request.setTransientMap(transientData);
            } catch (InvalidArgumentException e) {
                // Only happens if transientData is null
                throw new IllegalStateException(e);
            }
        }
        return request;
    }

    private void configureRequest(final User userContext, final TransactionRequest request, final String... args) {
        request.setChaincodeName(contractName);
        request.setFcn(name);
        request.setArgs(args);
        request.setUserContext(userContext);
    }

    private Collection<ProposalResponse> validatePeerResponses(final Collection<ProposalResponse> proposalResponses)
            throws ContractException {
        final Collection<ProposalResponse> validResponses = new ArrayList<>();
        final Collection<String> invalidResponseMsgs = new ArrayList<>();
        proposalResponses.forEach(response -> {
            if (response.getStatus().equals(ChaincodeResponse.Status.SUCCESS)) {
                validResponses.add(response);
            } else {
                invalidResponseMsgs.add(response.getMessage());
            }
        });

        if (validResponses.size() < 1) {
            String msg = String.format("No valid proposal responses received. %d peer error responses: %s",
                    invalidResponseMsgs.size(), String.join("; ", invalidResponseMsgs));
            throw new ContractException(msg, proposalResponses);
        }

        return validResponses;
    }

    public byte[] evaluate(final User userContext, final String... args) throws ContractException {
        QueryByChaincodeRequest request = newQueryRequest(userContext, args);
        Query query = new QueryImpl(channel, request);

        ProposalResponse response = queryHandler.evaluate(query);

        try {
            return response.getChaincodeActionResponsePayload();
        } catch (InvalidArgumentException e) {
            throw new ContractException(response.getMessage(), e);
        }
    }

    private QueryByChaincodeRequest newQueryRequest(final User userContext, final String... args) {
        QueryByChaincodeRequest request = client.newQueryProposalRequest();
        configureRequest(userContext, request, args);
        if (transientData != null) {
            try {
                request.setTransientMap(transientData);
            } catch (InvalidArgumentException e) {
                // Only happens if transientData is null
                throw new IllegalStateException(e);
            }
        }
        return request;
    }
}