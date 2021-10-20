package com.blkchainsolutions.fabric.client.impl.v1;

import java.nio.charset.StandardCharsets;

import com.blkchainsolutions.fabric.client.FabricClient;
import com.blkchainsolutions.fabric.client.FabricPayloadMapper;
import com.blkchainsolutions.fabric.client.exception.InvokeException;
import com.blkchainsolutions.fabric.client.exception.QueryException;
import com.blkchainsolutions.fabric.client.exception.SerializationException;
import com.blkchainsolutions.fabric.client.impl.v1.commit.CommitHandlerImpl;
import com.blkchainsolutions.fabric.client.param.QueryInvokeArgs;
import com.blkchainsolutions.fabric.client.param.QueryInvokeContext;
import com.blkchainsolutions.fabric.identity.WalletUtils;
import com.blkchainsolutions.fabric.identity.exception.IdentityNotFoundException;

import org.hyperledger.fabric.gateway.Identities;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.X509Identity;
import org.hyperledger.fabric.gateway.impl.TimePeriod;
import org.hyperledger.fabric.gateway.impl.identity.GatewayUser;
import org.hyperledger.fabric.gateway.spi.QueryHandler;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.identity.X509Enrollment;

import lombok.Builder;

/**
 * the default fabric service, this gateway will always use the default user to query
 */
@Builder
public final class FabricClientImpl implements FabricClient {

  // TODO check null
  private final HFClient client;
  private final Wallet wallet;
  private final String defaultUser;
  private final QueryHandler queryHandler;
  private final TimePeriod commitTimeout;
  private final boolean discoveryEnabled;
  private final String channelName;

  public String getUser(final QueryInvokeArgs args) throws IdentityNotFoundException {
    final String user = args.getContext().getUser();
    if (null != user) return user;
    if (null == defaultUser) throw new IdentityNotFoundException();
    return defaultUser;
  }

  public FabricTransaction createTransaction(final String contractName, final String functionName) {
    // Submit transactions that store state to the ledger.
    return FabricTransaction.builder()
      .client(client)
      .channel(client.getChannel(channelName))
      .contractName(contractName)
      .queryHandler(queryHandler)
      .commitTimeout(commitTimeout)
      .discoveryEnabled(discoveryEnabled)
      .commitHandlerFactory(CommitHandlerImpl::new)
      .name(functionName)
      .build();
  }

  @Override
  public String invoke(final QueryInvokeArgs args) throws InvokeException, IdentityNotFoundException {
    String user = getUser(args);
    final QueryInvokeContext context = args.getContext();

    FabricTransaction tx = createTransaction(context.getContractName(), args.getFunctionName());
    User userContext = WalletUtils.getUserContextFromWallet(wallet, user);

    try {
      byte[] result = tx.submit(userContext, args.getArguments());
      return new String(result, StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new InvokeException(e);
    }
  }

  @Override
  public <T> T invoke(final QueryInvokeArgs args, final FabricPayloadMapper<T> payloadMapper) throws SerializationException, InvokeException, IdentityNotFoundException {
    return payloadMapper.mapPayload(invoke(args));
  }

  @Override
  public String query(final QueryInvokeArgs args) throws QueryException, IdentityNotFoundException {
    String user = getUser(args);
    final QueryInvokeContext context = args.getContext();

    FabricTransaction tx = createTransaction(context.getContractName(), args.getFunctionName());
    User userContext = WalletUtils.getUserContextFromWallet(wallet, user);

    try {
      byte[] result = tx.evaluate(userContext, args.getArguments());
      return new String(result, StandardCharsets.UTF_8);
    } catch (Exception e) {
      throw new QueryException(e);
    }
  }

  @Override
  public <T> T query(final QueryInvokeArgs args, final FabricPayloadMapper<T> payloadMapper) throws SerializationException, QueryException, IdentityNotFoundException {
    return payloadMapper.mapPayload(query(args));
  }

  @Override
  public void shutdown(String name, boolean force) {
    client.getChannel(name).shutdown(force);
  }
  
}
