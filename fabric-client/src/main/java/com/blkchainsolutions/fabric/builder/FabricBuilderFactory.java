package com.blkchainsolutions.fabric.builder;

import com.blkchainsolutions.fabric.caclient.FabricCAClient;
import com.blkchainsolutions.fabric.caclient.impl.FabricCAClientBuilderImpl;
import com.blkchainsolutions.fabric.client.FabricClient;
import com.blkchainsolutions.fabric.client.impl.FabricClientBuilderImpl;

public class FabricBuilderFactory {

  private static FabricBuilderFactory provider;
  
  public static FabricBuilderFactory getInstance() {
    if (provider == null) {
      provider = new FabricBuilderFactory();
    }
    return provider;
  }

  public FabricCAClient.Builder getCABuilder() {
    return new FabricCAClientBuilderImpl();
  }

  public FabricClient.Builder getFabricBuilder() {
    return new FabricClientBuilderImpl();
  }
}
