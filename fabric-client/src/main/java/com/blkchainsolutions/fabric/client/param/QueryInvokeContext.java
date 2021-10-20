package com.blkchainsolutions.fabric.client.param;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class QueryInvokeContext {
  @lombok.NonNull private final String contractName;
  private final String user;
}
