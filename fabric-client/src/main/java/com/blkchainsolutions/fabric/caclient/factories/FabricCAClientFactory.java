package com.blkchainsolutions.fabric.caclient.factories;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import com.blkchainsolutions.fabric.caclient.FabricCAClient;
import com.blkchainsolutions.fabric.caclient.exceptions.CAClientCreationException;
import com.blkchainsolutions.fabric.caclient.impl.FabricCAClientImpl;
import com.blkchainsolutions.fabric.caclient.properties.FabricCAClientConfigProperties;
import com.blkchainsolutions.fabric.identity.WalletFactory;
import com.diffplug.common.base.Errors;

import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.sdk.NetworkConfig;
import org.hyperledger.fabric.sdk.NetworkConfig.CAInfo;
import org.hyperledger.fabric.sdk.NetworkConfig.OrgInfo;
import org.hyperledger.fabric.sdk.exception.NetworkConfigurationException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.hyperledger.fabric.sdk.security.CryptoSuiteFactory;
import org.hyperledger.fabric_ca.sdk.HFCAClient;

public class FabricCAClientFactory {

  static HFCAClient createSDKCAClient(CAInfo caInfo) throws CAClientCreationException {
    Properties properties = new Properties();
    // TODO check these properties are valid
    properties.setProperty("sslProvider", "openSSL");
    properties.setProperty("hostnameOverride", caInfo.getCAName());
    properties.setProperty("negotiateType", "TLS");

    try {
      CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault().getCryptoSuite(properties);
      return HFCAClient.createNewInstance(caInfo, cryptoSuite);
    } catch (Exception e) {
      throw new CAClientCreationException(e);
    }
  };

  private static Map<String, HFCAClient> createSDKCAClients(FabricCAClientConfigProperties config) throws CAClientCreationException {
    final String networkConfigPath = config.getNetworkProfilePath();
    final File file = new File(networkConfigPath);
    try {
      NetworkConfig networkConfig = NetworkConfig.fromJsonFile(file);
      OrgInfo organization = networkConfig.getClientOrganization();
      List<CAInfo> caInfos = organization.getCertificateAuthorities();

      return caInfos.stream().collect(Collectors.toMap(CAInfo::getName, Errors.rethrow().wrap(FabricCAClientFactory::createSDKCAClient)));
    } catch (NetworkConfigurationException | IOException e) {
      throw new CAClientCreationException("Error when reading network profile", e);
    }
  }

  public static void validateAdminNames(Map<String, String> adminNames, Set<String> clientKeys) {
    if (!adminNames.keySet().stream().allMatch(clientKeys::contains)) throw new IllegalArgumentException("The keys of admin names is not matched with client names in network config!");
  }

  public static FabricCAClient create(FabricCAClientConfigProperties config) throws CAClientCreationException {
    final FabricCAClientConfigProperties.FabricCAClientType type = config.getClientType();
    switch (type) {
      case FABRIC_SDK: {
        try {
          Map<String, HFCAClient> caClients = createSDKCAClients(config);
          Wallet wallet = WalletFactory.create(config.getWalletProperties());

          // validating admin names
          Map<String, String> adminNames = config.getAdminNames();
          validateAdminNames(adminNames, caClients.keySet());
          return new FabricCAClientImpl(caClients, wallet, adminNames);
        } catch (Exception e) {
          throw new CAClientCreationException(e);
        }
      }
      default: 
        throw new UnsupportedOperationException();
    }
  }
}
