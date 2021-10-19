package com.blkchainsolutions.fabric.caclient;

import java.util.Properties;

import com.blkchainsolutions.fabric.caclient.params.EnrollmentArgs;

import org.bouncycastle.est.EnrollmentResponse;

public interface FabricCAClient {

  /**
   * To enroll the admin
   * @param args
   */
  void enrollAdmin(EnrollmentArgs args);

  /**
   * To register user and put the id in the wallet
   * @param args arguments for enrolling the user
   * @return response from register user
   */
  EnrollmentResponse registerUser(EnrollmentArgs args);

  /**
   * To reenroll user and put the new id in the wallet (in case the certificates were expired)
   * @param args arguments for enrolling the user
   * @return response from reenrollment
   */
  EnrollmentResponse reenrollUser(EnrollmentArgs args);
}
