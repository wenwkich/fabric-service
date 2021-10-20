package com.blkchainsolutions.fabric.caclient.impl;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Map;

import com.blkchainsolutions.fabric.caclient.FabricCAClient;
import com.blkchainsolutions.fabric.caclient.exception.EnrollmentException;
import com.blkchainsolutions.fabric.caclient.exception.RegistrationException;
import com.blkchainsolutions.fabric.caclient.exception.UserAlreadyExistsException;
import com.blkchainsolutions.fabric.caclient.param.EnrollmentArgs;
import com.blkchainsolutions.fabric.caclient.param.RegisterArgs;
import com.blkchainsolutions.fabric.caclient.result.EnrollmentResult;
import com.blkchainsolutions.fabric.identity.WalletUtils;
import com.diffplug.common.base.Errors;

import org.hyperledger.fabric.gateway.Identities;
import org.hyperledger.fabric.gateway.Identity;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.X509Identity;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric_ca.sdk.Attribute;
import org.hyperledger.fabric_ca.sdk.EnrollmentRequest;
import org.hyperledger.fabric_ca.sdk.EnrollmentRequest.AttrReq;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FabricCAClientImpl implements FabricCAClient {

  private final Map<String, HFCAClient> clients;
  private final Wallet wallet;
  private final Map<String, String> adminNames;

  private static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
  private static final String END_CERT = "-----END CERTIFICATE-----";
  private static final String LINE_SEPARATOR = System.getProperty("line.separator");

  private void writeIdentityToWallet(String user, X509Identity x509Identity) throws EnrollmentException {
    if (null == x509Identity) throw new EnrollmentException("Unable to create identity");
    try {
      wallet.put(user, x509Identity);
    } catch (Exception e) {
      throw new EnrollmentException("Unable to write identity to wallet");
    }
  }

  private boolean checkUserExistsInWallet(String user) throws EnrollmentException {
    try {
      Identity admin = wallet.get(user);
      return null != admin;
    } catch (Exception e) {
      throw new EnrollmentException("Unable to check identity " + user + " to wallet");
    }
  }

  private X509Identity enrollUserHelper(EnrollmentArgs args) throws EnrollmentException {
    final String user = args.getEnrollmentId();
    final String secret = args.getSecret();
    final String caName = args.getCaName();
    final String mspId = args.getMspId();
    final Map<String, String> extendedAttr = args.getExtendedAttributes();

    // prepare enrollment request
    EnrollmentRequest request = new EnrollmentRequest();

    if (extendedAttr != null) {
      extendedAttr.keySet().forEach(Errors.rethrow().wrap(key -> {
        AttrReq req = request.addAttrReq(key);
        req.setOptional(false);
      }));
    }

    try {  
      HFCAClient client = clients.get(caName);
      Enrollment enrollment = client.enroll(user, secret, request);

      return Identities.newX509Identity(mspId, enrollment);
    } catch (Exception e) {
      throw new EnrollmentException(e);
    }
  }

  // returns the secret of register
  private String registerUserHelper(RegisterArgs args) throws RegistrationException {
    final String user = args.getEnrollmentId();
    final String secret = args.getSecret();
    final String caName = args.getCaName();
    final String affiliation = args.getAffiliation();
    final Map<String, String> extendedAttr = args.getExtendedAttributes();

    // prepare request to register
    RegistrationRequest request;
    try {
      request = new RegistrationRequest(user, affiliation);
    } catch (Exception e) {
      throw new RegistrationException("Unable to create registration request", e);
    }
    if (extendedAttr != null) {
      extendedAttr.forEach((key, value) -> {
        Attribute attr = new Attribute(key, value);
        request.addAttribute(attr);
      });
    }

    request.addAttribute(new Attribute("enrollmentID", user));
    request.addAttribute(new Attribute("enrollmentSecret", secret));
    request.addAttribute(new Attribute("role", "client"));

    // get admin user and register
    try {  
      HFCAClient client = clients.get(caName);
      final String adminName = adminNames.get(caName);

      User registarar = WalletUtils.getUserContextFromWallet(wallet, adminName);

      return client.register(request, registarar);

    } catch (Exception e) {
      throw new RegistrationException(e);
    }
  }

  @Override
  public void enrollUser(EnrollmentArgs args) throws EnrollmentException {
    final String user = args.getEnrollmentId();

    if (checkUserExistsInWallet(user)) return;
    writeIdentityToWallet(user, enrollUserHelper(args));
  }

  private String encodeCertHelper(X509Certificate cert) throws CertificateEncodingException {
    final Base64.Encoder encoder = Base64.getMimeEncoder(64, LINE_SEPARATOR.getBytes());

    StringBuilder builder = new StringBuilder();
    builder.append(BEGIN_CERT);
    final String encodedCertText = new String(encoder.encode(cert.getEncoded()));
    builder.append(encodedCertText);
    builder.append(END_CERT);
    return builder.toString();
  }

  @Override
  public EnrollmentResult registerUser(RegisterArgs args) throws EnrollmentException, RegistrationException, UserAlreadyExistsException, CertificateEncodingException {
    final String user = args.getEnrollmentId();
    if (checkUserExistsInWallet(user)) throw new UserAlreadyExistsException();

    // register user
    String secret = registerUserHelper(args);

    // enroll user
    EnrollmentArgs enrollmentArgs = EnrollmentArgs.builder()
      .caName(args.getCaName())
      .enrollmentId(args.getEnrollmentId())
      .extendedAttributes(args.getExtendedAttributes())
      .mspId(args.getMspId())
      .secret(secret)
      .build();
    X509Identity x509Identity = enrollUserHelper(enrollmentArgs);

    // write user to wallet
    writeIdentityToWallet(user, x509Identity);

    // prepare result
    return EnrollmentResult.builder()
      .cert(encodeCertHelper(x509Identity.getCertificate()))
      .privateKey(x509Identity.getPrivateKey().toString())
      .secret(secret)
      .build();
  }

  private X509Identity reenrollUserHelper(EnrollmentArgs args) throws EnrollmentException {
    final String user = args.getEnrollmentId();
    final String secret = args.getSecret();
    final String caName = args.getCaName();
    final String mspId = args.getMspId();
    final Map<String, String> extendedAttr = args.getExtendedAttributes();

    // prepare enrollment request
    EnrollmentRequest request = new EnrollmentRequest();

    if (extendedAttr != null) {
      extendedAttr.keySet().forEach(Errors.rethrow().wrap(key -> {
        AttrReq req = request.addAttrReq(key);
        req.setOptional(false);
      }));
    }

    try {  
      User userContext = WalletUtils.getUserContextFromWallet(wallet, user);
      HFCAClient client = clients.get(caName);
      Enrollment enrollment = client.reenroll(userContext, request);

      return Identities.newX509Identity(mspId, enrollment);
    } catch (Exception e) {
      throw new EnrollmentException(e);
    }
  }

  @Override
  public EnrollmentResult reenrollUser(EnrollmentArgs args) throws EnrollmentException, RegistrationException, CertificateEncodingException {
    final String user = args.getEnrollmentId();

    X509Identity x509Identity = reenrollUserHelper(args);

    // write user to wallet
    writeIdentityToWallet(user, x509Identity);

    // prepare result
    return EnrollmentResult.builder()
      .cert(encodeCertHelper(x509Identity.getCertificate()))
      .privateKey(x509Identity.getPrivateKey().toString())
      .secret(args.getSecret())
      .build();
  }


}
