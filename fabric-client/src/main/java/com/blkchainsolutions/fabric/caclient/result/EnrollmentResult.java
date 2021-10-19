package com.blkchainsolutions.fabric.caclient.result;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
class EnrollmentResult {
  @ToString.Exclude private String privateKey;
  private String cert;
  private String username;
  @ToString.Exclude private String secret;
}
