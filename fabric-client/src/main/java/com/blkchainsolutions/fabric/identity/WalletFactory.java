package com.blkchainsolutions.fabric.identity;

import java.io.IOException;
import java.nio.file.Paths;

import com.blkchainsolutions.fabric.client.properties.WalletConfigProperties;
import com.blkchainsolutions.fabric.identity.exceptions.WalletCreationException;

import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;

public class WalletFactory {
  public static Wallet create(WalletConfigProperties config) throws WalletCreationException {
    final WalletConfigProperties.WalletType type = config.getWalletType();
    switch (type) {
      case FILE_SYSTEM: {
        final String walletPath = config.getWalletPath();
        if (null == walletPath) throw new IllegalArgumentException("Wallet path is not set");
        try {
          return Wallets.newFileSystemWallet(Paths.get(walletPath));
        } catch (IOException e) {
          throw new WalletCreationException(e);
        }
      }
      default: 
        throw new UnsupportedOperationException();
    }
  }
}
