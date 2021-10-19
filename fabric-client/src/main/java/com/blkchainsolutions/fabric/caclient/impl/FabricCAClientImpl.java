package com.blkchainsolutions.fabric.caclient.impl;

import java.util.Map;
import java.util.Properties;

import com.blkchainsolutions.fabric.caclient.FabricCAClient;
import com.blkchainsolutions.fabric.caclient.params.EnrollmentArgs;

import org.bouncycastle.est.EnrollmentResponse;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric_ca.sdk.HFCAClient;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FabricCAClientImpl implements FabricCAClient {

  private final Map<String, HFCAClient> clients;
  private final Wallet wallet;
  private final Map<String, String> adminNames;

  @Override
  public void enrollAdmin(EnrollmentArgs args) {
  }

  @Override
  public EnrollmentResponse registerUser(EnrollmentArgs args) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public EnrollmentResponse reenrollUser(EnrollmentArgs args) {
    // TODO Auto-generated method stub
    return null;
  }


}
