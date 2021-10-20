package com.blkchainsolutions.fabric.caclient.param;

import java.util.Map;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@ToString
@Getter
public class EnrollmentArgs {
  private String caName;
  private String enrollmentId;
  @ToString.Exclude private String secret;
  private Map<String, String> extendedAttributes;
  private String mspId;
}
