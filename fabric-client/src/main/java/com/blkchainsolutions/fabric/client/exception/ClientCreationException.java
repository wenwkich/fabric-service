package com.blkchainsolutions.fabric.client.exception;

public class ClientCreationException extends Exception {

  public ClientCreationException() {
    super("Error when creating client");
  }

  public ClientCreationException(String message) {
    super(message);
  }

  public ClientCreationException(String message, Exception e) {
    super(message, e);
  }
  
  public ClientCreationException(Exception e) {
    super("Error when creating client", e);
  }
  
}
