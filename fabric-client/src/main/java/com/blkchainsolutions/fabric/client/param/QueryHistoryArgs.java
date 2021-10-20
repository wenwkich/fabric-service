package com.blkchainsolutions.fabric.client.param;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class QueryHistoryArgs {
  @lombok.NonNull private final QueryInvokeContext context;
  @lombok.NonNull private final String key;
  @lombok.NonNull private final String queryString;
}
