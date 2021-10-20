package com.blkchainsolutions.fabric.caclient;

import java.security.cert.CertificateEncodingException;
import java.util.Map;

import com.blkchainsolutions.fabric.caclient.exception.CAClientCreationException;
import com.blkchainsolutions.fabric.caclient.exception.EnrollmentException;
import com.blkchainsolutions.fabric.caclient.exception.RegistrationException;
import com.blkchainsolutions.fabric.caclient.exception.UserAlreadyExistsException;
import com.blkchainsolutions.fabric.caclient.param.EnrollmentArgs;
import com.blkchainsolutions.fabric.caclient.param.RegisterArgs;
import com.blkchainsolutions.fabric.caclient.result.EnrollmentResult;
import com.blkchainsolutions.fabric.identity.WalletConfigProperties;

public interface FabricCAClient {

  /**
   * To enroll the user, normally for admins
   * @param args
   * @throws EnrollmentException
   */
  void enrollUser(EnrollmentArgs args) throws EnrollmentException;

  /**
   * To register user and put the id in the wallet
   * @param args arguments for enrolling the user
   * @return response from register user
   * @throws RegistrationException
   * @throws EnrollmentException
   * @throws UserAlreadyExistsException
   * @throws CertificateEncodingException
   */
  EnrollmentResult registerUser(RegisterArgs args) throws EnrollmentException, RegistrationException, UserAlreadyExistsException, CertificateEncodingException;

  /**
   * To reenroll user and put the new id in the wallet (in case the certificates were expired)
   * @param args arguments for enrolling the user
   * @return response from reenrollment
   * @throws RegistrationException
   * @throws EnrollmentException
   * @throws CertificateEncodingException
   */
  EnrollmentResult reenrollUser(EnrollmentArgs args) throws EnrollmentException, RegistrationException, CertificateEncodingException;

  public interface Builder {

    // TODO write documentation about   

    Builder networkProfilePath(String networkProfilePath);
    Builder walletProperties(WalletConfigProperties walletProperties);
    Builder adminNames(Map<String, String> adminNames);
    
    FabricCAClient build() throws CAClientCreationException;
  }
}
