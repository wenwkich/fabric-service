package com.blkchainsolutions.fabric.client;

import java.util.concurrent.TimeUnit;

import com.blkchainsolutions.fabric.client.exception.ClientCreationException;
import com.blkchainsolutions.fabric.identity.WalletConfigProperties;

public interface FabricClient extends 
  Invoke, Query {

  /**
   * shutdown the application
   * @param name the name of the network name
   * @param force force shutdown, note that it does not work with gateway java impl
   */
  public void shutdown(String name, boolean force);

  public interface Builder {
    public FabricClient build() throws ClientCreationException;
    public Builder networkProfilePath(String networkProfilePath);
    public Builder adminUser(String adminUser);
    public Builder walletProperties(WalletConfigProperties walletProperties);
    public Builder channelName(String channelName);
    public Builder commitTime(long commitTime);
    public Builder commitTimeUnit(TimeUnit commitTimeUnit);
  }
}
