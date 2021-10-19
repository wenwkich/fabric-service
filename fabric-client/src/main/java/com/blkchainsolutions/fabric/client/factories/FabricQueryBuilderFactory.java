package com.blkchainsolutions.fabric.client.factories;

import com.blkchainsolutions.fabric.client.FabricQueryBuilder;
import com.blkchainsolutions.fabric.client.impl.FabricQueryBuilderImpl;

public class FabricQueryBuilderFactory {

  public static FabricQueryBuilder create() {
    return new FabricQueryBuilderImpl();
  }

}
