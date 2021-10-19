package com.blkchainsolutions.fabric.identity.exceptions;

public class IdentityNotFoundException extends Exception {

  public IdentityNotFoundException() {
    super("No identity is found in the wallet, please make sure you have enrolled the user");
  }

  public IdentityNotFoundException(String message) {
    super(message);
  }
  
}
