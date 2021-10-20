package com.blkchainsolutions.fabric.caclient.exception;

public class EnrollmentException extends Exception {

  public EnrollmentException() {
    super("Unable to enroll user");
  }

  public EnrollmentException(String message) {
    super(message);
  }

  public EnrollmentException(String message, Exception e) {
    super(message, e);
  }

  public EnrollmentException(Exception e) {
    super("Unable to enroll user", e);
  }

}
