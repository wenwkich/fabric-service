package com.blkchainsolutions.fabric.caclient.impl;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import com.blkchainsolutions.fabric.caclient.FabricCAClient;
import com.blkchainsolutions.fabric.caclient.FabricCAClient.Builder;
import com.blkchainsolutions.fabric.caclient.exception.CAClientCreationException;
import com.blkchainsolutions.fabric.identity.WalletConfigProperties;
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

public class FabricCAClientBuilderImpl implements FabricCAClient.Builder {

  private String networkProfilePath;
  private WalletConfigProperties walletProperties;
  private Map<String, String> adminNames;

  private HFCAClient createSDKCAClient(CAInfo caInfo) throws CAClientCreationException {
    Properties property = new Properties();
    // TODO check these property are valid
    property.setProperty("sslProvider", "openSSL");
    property.setProperty("hostnameOverride", caInfo.getCAName());
    property.setProperty("negotiateType", "TLS");

    try {
      CryptoSuite cryptoSuite = CryptoSuiteFactory.getDefault().getCryptoSuite(property);
      return HFCAClient.createNewInstance(caInfo, cryptoSuite);
    } catch (Exception e) {
      throw new CAClientCreationException(e);
    }
  }

  private Map<String, HFCAClient> createSDKCAClients(final String networkConfigPath) throws CAClientCreationException {
    final File file = new File(networkConfigPath);
    try {
      NetworkConfig networkConfig = NetworkConfig.fromJsonFile(file);
      OrgInfo organization = networkConfig.getClientOrganization();
      List<CAInfo> caInfos = organization.getCertificateAuthorities();

      return caInfos.stream().collect(Collectors.toMap(CAInfo::getName, Errors.rethrow().wrap(this::createSDKCAClient)));
    } catch (NetworkConfigurationException | IOException e) {
      throw new CAClientCreationException("Error when reading network profile", e);
    }
  }

  public void validateAdminNames(Map<String, String> adminNames, Set<String> clientKeys) {
    if (!adminNames.keySet().stream().allMatch(clientKeys::contains)) throw new IllegalArgumentException("The keys of admin names is not matched with client names in network config!");
  }

  @Override
  public FabricCAClient build() throws CAClientCreationException {
    try {
      Map<String, HFCAClient> caClients = createSDKCAClients(networkProfilePath);
      Wallet wallet = WalletFactory.create(walletProperties);

      // validating admin names
      validateAdminNames(adminNames, caClients.keySet());
      return new FabricCAClientImpl(caClients, wallet, adminNames);
    } catch (Exception e) {
      throw new CAClientCreationException(e);
    }
  }

  @Override
  public Builder networkProfilePath(String networkProfilePath) {
    this.networkProfilePath = networkProfilePath;
    return this;
  }

  @Override
  public Builder walletProperties(WalletConfigProperties walletProperties) {
    this.walletProperties = walletProperties;
    return this;
  }

  @Override
  public Builder adminNames(Map<String, String> adminNames) {
    this.adminNames = adminNames;
    return this;
  }
}
