package com.blkchainsolutions.fabric.identity.exceptions;

public class WalletCreationException extends Exception {

  public WalletCreationException() {
    super("Error creating wallet");
  }

  public WalletCreationException(String message) {
    super(message);
  }

  public WalletCreationException(String message, Exception e) {
    super(message, e);
  }

  public WalletCreationException(Exception e) {
    super("Error creating wallet", e);
  }
  
}
