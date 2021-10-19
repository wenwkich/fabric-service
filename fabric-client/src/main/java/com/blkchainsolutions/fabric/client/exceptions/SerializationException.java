package com.blkchainsolutions.fabric.client.exceptions;

public class SerializationException extends Exception {
  
  public SerializationException() {
    super("Error during serialization");
  }

  public SerializationException(String message) {
    super(message);
  }

  public SerializationException(String message, Exception e) {
    super(message, e);
  }

  public SerializationException(Exception e) {
    super("Error during serialization", e);
  }

}
