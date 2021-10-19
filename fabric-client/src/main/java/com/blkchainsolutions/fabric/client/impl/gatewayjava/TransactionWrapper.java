package com.blkchainsolutions.fabric.client.impl.gatewayjava;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.blkchainsolutions.fabric.client.exceptions.InvokeException;
import com.blkchainsolutions.fabric.client.exceptions.QueryException;
import com.blkchainsolutions.fabric.client.exceptions.ReflectionCallException;

import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.GatewayRuntimeException;
import org.hyperledger.fabric.gateway.Identities;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.Transaction;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.X509Identity;
import org.hyperledger.fabric.gateway.impl.identity.GatewayUser;
import org.hyperledger.fabric.gateway.impl.query.QueryImpl;
import org.hyperledger.fabric.gateway.spi.CommitHandlerFactory;
import org.hyperledger.fabric.gateway.spi.Query;
import org.hyperledger.fabric.gateway.spi.QueryHandler;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.QueryByChaincodeRequest;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;
import org.hyperledger.fabric.sdk.TransactionRequest;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.ServiceDiscoveryException;
import org.hyperledger.fabric.sdk.identity.X509Enrollment;

public class TransactionWrapper implements Transaction {
  
  private final Transaction transaction;

  public TransactionWrapper(Transaction transaction) {
    this.transaction = transaction;
  }

  @Override
  public String getName() {
    return transaction.getName();
  }

  @Override
  public String getTransactionId() {
    return transaction.getTransactionId();
  }

  @Override
  public Transaction setTransient(Map<String, byte[]> transientData) {
    return transaction.setTransient(transientData);
  }

  @Override
  public Transaction setCommitTimeout(long timeout, TimeUnit timeUnit) {
    return transaction.setCommitTimeout(timeout, timeUnit);
  }

  @Override
  public Transaction setCommitHandler(CommitHandlerFactory commitHandler) {
    return transaction.setCommitHandler(commitHandler);
  }

  @Override
  public Transaction setEndorsingPeers(Collection<Peer> peers) {
    return transaction.setEndorsingPeers(peers);
  }

  @Override
  public byte[] submit(String... args) throws ContractException, TimeoutException, InterruptedException {
    return transaction.submit(args);
  }

  @Override
  public byte[] evaluate(String... args) throws ContractException {
    return transaction.evaluate(args);
  }
  
  public byte[] submit(Wallet wallet, String label, String ...args) throws ContractException, TimeoutException, InterruptedException, InvokeException {
    Collection<ProposalResponse> proposalResponses = endorseTransaction(wallet, label, args);

    try {
      // validate peer response
      Collection<ProposalResponse> validResponses = ReflectionUtils.invokePrivateMethod(transaction, "validatePeerResponses", proposalResponses);

      // commit transaction
      return ReflectionUtils.invokePrivateMethod(transaction, "commitTransaction", validResponses);
    } catch (Exception e) {
      if (e instanceof ContractException) {
        ContractException contractException = (ContractException) e;
        contractException.setProposalResponses(proposalResponses);
      }
      throw new InvokeException(e);
    }
  }

  private Collection<ProposalResponse> endorseTransaction(final Wallet wallet, final String label, final String... args) throws InvokeException {
    try {
      TransactionProposalRequest request = newProposalRequest(wallet, label, args);

      return ReflectionUtils.invokePrivateMethod(transaction, "sendTransactionProposal", request);
    } catch (Exception e) {
      if (e instanceof InvalidArgumentException || e instanceof ProposalException || e instanceof ServiceDiscoveryException) {
        throw new GatewayRuntimeException(e);
      }
      throw new InvokeException(e);
    }
  }

  private Gateway getGateway() throws InvokeException {
    try {
      // get the network first
      Network network = ReflectionUtils.getPrivateField(transaction, "network");
  
      // get the gateway
      return network.getGateway();
    } catch (Exception e) {
      throw new InvokeException(e);
    }
  }

  private TransactionProposalRequest newProposalRequest(final Wallet wallet, String label, final String... args) throws InvokeException {
    Gateway gateway = getGateway();

    Map<String, byte[]> transientData;
    TransactionProposalRequest request;
    try {
      // get the client from gateway
      HFClient client = ReflectionUtils.invokePrivateMethod(gateway, "getClient");
      request = client.newTransactionProposalRequest();

      configureRequest(wallet, label, request, args);

      // get the transient field
      transientData = ReflectionUtils.getPrivateField(transaction, "transientData");
    } catch (Exception e) {
      throw new InvokeException(e);
    }

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

  private void configureRequest(final Wallet wallet, final String label, final TransactionRequest request, final String... args) throws ProposalException {
    try { 
      Contract contract = ReflectionUtils.getPrivateField(transaction, "contract");
      String chaincodeId = ReflectionUtils.invokePrivateMethod(contract, "getChaincodeId");

      request.setChaincodeName(chaincodeId);
      request.setFcn(transaction.getName());
      request.setArgs(args);
    } catch (Exception e) {
      throw new ProposalException(e);
    }

    // set user context
    X509Identity x509Identity;
    try {
      x509Identity = (X509Identity) wallet.get(label);
    } catch (IOException e) {
      throw new ProposalException("IO Error when getting identity from wallet", e);
    }

    String certificatePem = Identities.toPemString(x509Identity.getCertificate());
    Enrollment enrollment = new X509Enrollment(x509Identity.getPrivateKey(), certificatePem);
    User user = new GatewayUser(label, x509Identity.getMspId(), enrollment);
    // call request set user context
    request.setUserContext(user);
  }

  public byte[] evaluate(final Wallet wallet, final String label, final String... args) throws ContractException, QueryException {
    QueryByChaincodeRequest request = newQueryRequest(wallet, label, args);
    ProposalResponse response = null;
    try {
      Network network = ReflectionUtils.getPrivateField(transaction, "network");
      Query query = new QueryImpl(network.getChannel(), request);

      QueryHandler queryHandler = ReflectionUtils.getPrivateField(transaction, "queryHandler");
      response = queryHandler.evaluate(query);
      return response.getChaincodeActionResponsePayload();
    } catch (InvalidArgumentException e) {
      if (null == response) throw new QueryException(e);
      throw new ContractException(response.getMessage(), e);
    } catch (ReflectionCallException e) {
      throw new QueryException(e);
    }
  }

  private QueryByChaincodeRequest newQueryRequest(final Wallet wallet, final String label, final String... args) throws QueryException {
    Map<String, byte[]> transientData;
    QueryByChaincodeRequest request;
    try {
      Gateway gateway = getGateway();
      HFClient client = ReflectionUtils.invokePrivateMethod(gateway, "getClient");
      request = client.newQueryProposalRequest();
      configureRequest(wallet, label, request, args);
      transientData = ReflectionUtils.getPrivateField(transaction, "transientData");
    } catch (Exception e) {
      throw new QueryException(e);
    }

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

  static class ReflectionUtils {
    public static <T, R> R invokePrivateMethod(Object invoked, String methodName, Object... arguments) throws ReflectionCallException {
      try {
        Method method = invoked.getClass().getDeclaredMethod(methodName);
        method.setAccessible(true);
        return (R) method.invoke(invoked, arguments);
      } catch (Exception e) {
        throw new ReflectionCallException(e);
      }
    }

    public static <T, R> R getPrivateField(Object invoked, String fieldName) throws ReflectionCallException {
      try {
        Field field = invoked.getClass().getField(fieldName);
        field.setAccessible(true);
        return (R) field.get(invoked);
      } catch (Exception e) {
        throw new ReflectionCallException(e);
      }
    }
  }
  
}
