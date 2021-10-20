package com.blkchainsolutions.fabric.caclient.exception;

public class RegistrationException extends Exception {

  public RegistrationException() {
    super("Unable to register user");
  }

  public RegistrationException(String message) {
    super(message);
  }

  public RegistrationException(String message, Exception e) {
    super(message, e);
  }

  public RegistrationException(Exception e) {
    super("Unable to register user", e);
  }

}
