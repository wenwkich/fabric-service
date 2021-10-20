package com.blkchainsolutions.fabric.caclient.result;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class EnrollmentResult {
  @ToString.Exclude private String privateKey;
  private String cert;
  @ToString.Exclude private String secret;
}
