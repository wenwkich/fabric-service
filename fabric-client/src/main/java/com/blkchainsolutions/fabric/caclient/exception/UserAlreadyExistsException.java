package com.blkchainsolutions.fabric.caclient.exception;

public class UserAlreadyExistsException extends Exception {

  public UserAlreadyExistsException() {
    super("User already exists in wallet");
  }

  public UserAlreadyExistsException(String message) {
    super(message);
  }

  public UserAlreadyExistsException(String message, Exception e) {
    super(message, e);
  }

  public UserAlreadyExistsException(Exception e) {
    super("User already exists in wallet", e);
  }

}
