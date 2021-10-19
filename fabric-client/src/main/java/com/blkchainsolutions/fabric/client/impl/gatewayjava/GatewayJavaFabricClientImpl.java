package com.blkchainsolutions.fabric.client.impl.gatewayjava;

import com.blkchainsolutions.fabric.client.FabricClient;
import com.blkchainsolutions.fabric.client.FabricPayloadMapper;
import com.blkchainsolutions.fabric.client.exceptions.InvokeException;
import com.blkchainsolutions.fabric.client.exceptions.QueryException;
import com.blkchainsolutions.fabric.client.exceptions.SerializationException;
import com.blkchainsolutions.fabric.client.params.QueryInvokeArgs;
import com.blkchainsolutions.fabric.identity.exceptions.IdentityNotFoundException;

import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Wallet;

import lombok.RequiredArgsConstructor;

/**
 * the default fabric service, this gateway will always use the default user to query
 */
@RequiredArgsConstructor
public final class GatewayJavaFabricClientImpl implements FabricClient {

  private final Gateway gateway;
  private final Wallet wallet;
  private final String defaultUser;

  public String getUser(final QueryInvokeArgs args) throws IdentityNotFoundException {
    final String user = args.getContext().getUser();
    if (null != user) return user;
    if (null == defaultUser) throw new IdentityNotFoundException();
    return defaultUser;
  }

  @Override
  public String invoke(final QueryInvokeArgs args) throws InvokeException, IdentityNotFoundException {
    String user = getUser(args);
    return (new InvokeImpl(gateway, wallet, user)).invoke(args);
  }

  @Override
  public <T> T invoke(final QueryInvokeArgs args, final FabricPayloadMapper<T> payloadMapper) throws SerializationException, InvokeException, IdentityNotFoundException {
    return payloadMapper.mapPayload(invoke(args));
  }

  @Override
  public String query(final QueryInvokeArgs args) throws QueryException, IdentityNotFoundException {
    String user = getUser(args);
    return (new QueryImpl(gateway, wallet, user)).query(args);
  }

  @Override
  public <T> T query(final QueryInvokeArgs args, final FabricPayloadMapper<T> payloadMapper) throws SerializationException, QueryException, IdentityNotFoundException {
    return payloadMapper.mapPayload(query(args));
  }

  @Override
  public void shutdown(boolean force) {
    gateway.close();
  }
  
}
