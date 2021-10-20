package com.blkchainsolutions.fabric.client.impl;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;

import com.blkchainsolutions.fabric.client.FabricClient;
import com.blkchainsolutions.fabric.client.FabricClient.Builder;
import com.blkchainsolutions.fabric.client.exception.ClientCreationException;
import com.blkchainsolutions.fabric.client.impl.v1.FabricClientImpl;
import com.blkchainsolutions.fabric.identity.WalletConfigProperties;
import com.blkchainsolutions.fabric.identity.WalletFactory;

import org.hyperledger.fabric.gateway.Identity;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.impl.TimePeriod;
import org.hyperledger.fabric.gateway.impl.query.RoundRobinQueryHandler;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.NetworkConfig;
import org.hyperledger.fabric.sdk.Peer.PeerRole;

public class FabricClientBuilderImpl implements FabricClient.Builder {

  private String networkProfilePath;
  private String adminUser;
  private WalletConfigProperties walletProperties;
  private String channelName;
  private long commitTime;
  private TimeUnit commitTimeUnit;
  private boolean discoveryEnabled;

  private HFClient newHfClient() throws ClientCreationException {
    try {
      Path networkConfigFile = Paths.get(networkProfilePath);
      NetworkConfig networkConfig = NetworkConfig.fromJsonFile(networkConfigFile.toFile());
      HFClient client = HFClient.createNewInstance();
      client.loadChannelFromConfig(channelName, networkConfig);
      return client;
    } catch (Exception e) {
      throw new ClientCreationException(e);
    }
  }

  @Override
  public FabricClient build() throws ClientCreationException {
    long nonZeroCommitTime = 0 == this.commitTime ? 15 : this.commitTime;
    TimeUnit nonNullTimeUnit = null == this.commitTimeUnit ? TimeUnit.SECONDS : this.commitTimeUnit;
    HFClient client = newHfClient();
    try {
      Wallet wallet = WalletFactory.create(walletProperties);

      Identity identity = wallet.get(adminUser);
      if (null == identity) throw new ClientCreationException("Could not get default from wallet");

      return FabricClientImpl.builder()
        .client(client)
        .channelName(channelName)
        .commitTimeout(new TimePeriod(nonZeroCommitTime, nonNullTimeUnit))
        .defaultUser(adminUser)
        .discoveryEnabled(discoveryEnabled)
        .wallet(wallet)
        .queryHandler(
          new RoundRobinQueryHandler(
            client.getChannel(channelName)
            .getPeers(EnumSet.of(
              PeerRole.LEDGER_QUERY, 
              PeerRole.CHAINCODE_QUERY
            ))))
        .build();
    } catch (Exception e) {
      throw new ClientCreationException(e);
    }
  }

  @Override
  public Builder networkProfilePath(String networkProfilePath) {
    this.networkProfilePath = networkProfilePath;
    return this;
  }

  @Override
  public Builder adminUser(String adminUser) {
    this.adminUser = adminUser;
    return this;
  }

  @Override
  public Builder walletProperties(WalletConfigProperties walletProperties) {
    this.walletProperties = walletProperties;
    return this;
  }

  @Override
  public Builder channelName(String channelName) {
    this.channelName = channelName;
    return this;
  }

  @Override
  public Builder commitTime(long commitTime) {
    this.commitTime = commitTime;
    return this;
  }

  @Override
  public Builder commitTimeUnit(TimeUnit commitTimeUnit) {
    this.commitTimeUnit = commitTimeUnit;
    return this;
  }
  
}
