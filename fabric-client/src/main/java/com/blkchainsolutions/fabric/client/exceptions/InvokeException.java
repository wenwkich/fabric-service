package com.blkchainsolutions.fabric.client.exceptions;

public class InvokeException extends Exception {
  
  public InvokeException() {
    super("Error during contract invocation");
  }

  public InvokeException(String message) {
    super(message);
  }

  public InvokeException(String message, Exception e) {
    super(message, e);
  }

  public InvokeException(Exception e) {
    super("Error during contract invocation", e);
  }

}
