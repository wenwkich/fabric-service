package com.blkchainsolutions.fabric.client.params;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class QueryInvokeArgs {
  @lombok.NonNull private final QueryInvokeContext context;
  @lombok.NonNull private final String functionName;
  @lombok.NonNull private final String[] arguments;
}