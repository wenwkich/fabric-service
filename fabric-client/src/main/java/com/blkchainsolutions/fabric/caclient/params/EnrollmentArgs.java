package com.blkchainsolutions.fabric.caclient.params;

import java.util.Properties;

import lombok.Builder;
import lombok.ToString;

@Builder
@ToString
public class EnrollmentArgs {
  private String caName;
  private String enrollmentId;
  @ToString.Exclude private String secret;
  private Properties extendedAttributes;
}
