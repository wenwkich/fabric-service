package com.blkchainsolutions.fabric.client.properties;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class FabricClientConfigProperties {
  @lombok.NonNull private final FabricClientType clientType;
  @lombok.NonNull private final String networkProfilePath;
  @lombok.NonNull private final String adminUser;
  @lombok.NonNull private final WalletConfigProperties walletProperties;

  public enum FabricClientType { FABRIC_GATEWAY, FABRIC_SDK }

  private String walletPath;
}
