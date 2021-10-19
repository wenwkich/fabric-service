package com.blkchainsolutions.fabric.client.factories;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import com.blkchainsolutions.fabric.client.FabricClient;
import com.blkchainsolutions.fabric.client.exceptions.ClientCreationException;
import com.blkchainsolutions.fabric.client.impl.gatewayjava.GatewayJavaFabricClientImpl;
import com.blkchainsolutions.fabric.client.properties.FabricClientConfigProperties;
import com.blkchainsolutions.fabric.client.properties.WalletConfigProperties;
import com.blkchainsolutions.fabric.identity.WalletFactory;

import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.impl.query.RoundRobinQueryHandler;

public class FabricClientFactory {

  private static Gateway createGateway(FabricClientConfigProperties config, Wallet wallet) throws ClientCreationException {
    final String networkConfigPath = config.getNetworkProfilePath();
    // path to a common connection profile describing the network.
    Path networkConfigFile = Paths.get(networkConfigPath);

    // configure the gateway connection used to access the network.
    try {
      Gateway.Builder builder = Gateway.createBuilder()
        .identity(wallet, config.getAdminUser())
        .discovery(true)
        .queryHandler((network) -> new RoundRobinQueryHandler(network.getChannel().getPeers()))
        .commitTimeout(3, TimeUnit.SECONDS)
        .networkConfig(networkConfigFile);
      return builder.connect();
    } catch (IOException e) {
      throw new ClientCreationException("Failed to build client", e);
    }
  }

  public static FabricClient create(FabricClientConfigProperties config) throws ClientCreationException {
    final FabricClientConfigProperties.FabricClientType type = config.getClientType();

    switch (type) {
      case FABRIC_GATEWAY: {
        try {
          Wallet wallet = WalletFactory.create(config.getWalletProperties());
          Gateway gateway = createGateway(config, wallet);
          return new GatewayJavaFabricClientImpl(gateway, wallet, config.getAdminUser());
        } catch (Exception e) {
          throw new ClientCreationException(e);
        }
      }
      default: 
        throw new UnsupportedOperationException();
    }
  }
  
}
