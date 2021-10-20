package com.blkchainsolutions.fabric.caclient.exception;

public class CAClientCreationException extends Exception {

  public CAClientCreationException() {
    super("Unable to create HFCAClients");
  }

  public CAClientCreationException(String message) {
    super(message);
  }

  public CAClientCreationException(String message, Exception e) {
    super(message, e);
  }

  public CAClientCreationException(Exception e) {
    super("Unable to create HFCAClients", e);
  }

}
