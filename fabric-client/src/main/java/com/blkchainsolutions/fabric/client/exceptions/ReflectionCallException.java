package com.blkchainsolutions.fabric.client.exceptions;

public class ReflectionCallException extends Exception {
  
  public ReflectionCallException() {
    super("Error during reflection call");
  }

  public ReflectionCallException(String message) {
    super(message);
  }

  public ReflectionCallException(String message, Exception e) {
    super(message, e);
  }

  public ReflectionCallException(Exception e) {
    super("Error during reflection call", e);
  }

}
