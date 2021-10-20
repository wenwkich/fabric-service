package com.blkchainsolutions.fabric.identity.exception;

public class IdentityNotFoundException extends Exception {

  public IdentityNotFoundException() {
    super("No identity is found in the wallet, please make sure you have enrolled the user");
  }

  public IdentityNotFoundException(String message) {
    super(message);
  }

  public IdentityNotFoundException(String message, Exception e) {
    super(message, e);
  }
  
  public IdentityNotFoundException(Exception e) {
    super("No identity is found in the wallet, please make sure you have enrolled the user", e);
  }
}
