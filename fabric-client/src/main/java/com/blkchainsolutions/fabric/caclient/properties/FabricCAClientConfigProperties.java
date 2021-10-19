package com.blkchainsolutions.fabric.caclient.properties;

import java.util.Map;

import com.blkchainsolutions.fabric.client.properties.WalletConfigProperties;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class FabricCAClientConfigProperties {
  @lombok.NonNull private final FabricCAClientType clientType;
  @lombok.NonNull private final String networkProfilePath;
  @lombok.NonNull private final WalletConfigProperties walletProperties;
  @lombok.NonNull private final Map<String, String> adminNames;

  public enum FabricCAClientType { FABRIC_SDK }

}
