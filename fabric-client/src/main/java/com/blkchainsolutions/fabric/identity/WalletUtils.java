package com.blkchainsolutions.fabric.identity;

import com.blkchainsolutions.fabric.identity.exception.IdentityNotFoundException;

import org.hyperledger.fabric.gateway.Identities;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.X509Identity;
import org.hyperledger.fabric.gateway.impl.identity.GatewayUser;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.identity.X509Enrollment;

public class WalletUtils {
  public static User getUserContextFromWallet(Wallet wallet, String user) throws IdentityNotFoundException {
    X509Identity x509Identity = null;
    try {
      // get user from wallet
      x509Identity = (X509Identity) wallet.get(user);
    } catch (Exception e) {
      throw new IdentityNotFoundException("Identity " + user + " could not be found from the wallet", e);
    }

    if (null == x509Identity) throw new IdentityNotFoundException("Identity " + user + " could not be found from the wallet");
    String certificatePem = Identities.toPemString(x509Identity.getCertificate());
    Enrollment enrollment = new X509Enrollment(x509Identity.getPrivateKey(), certificatePem);
    return new GatewayUser(user, x509Identity.getMspId(), enrollment);
  }
}
