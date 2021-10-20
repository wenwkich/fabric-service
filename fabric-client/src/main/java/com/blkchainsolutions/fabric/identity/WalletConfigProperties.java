package com.blkchainsolutions.fabric.identity;



import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class WalletConfigProperties {
  public enum WalletType { FILE_SYSTEM, IN_MEMORY, COUCH_DB }

  private final String walletPath;
  @lombok.NonNull private final WalletType walletType;

}
