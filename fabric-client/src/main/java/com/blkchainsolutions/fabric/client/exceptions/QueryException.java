package com.blkchainsolutions.fabric.client.exceptions;

public class QueryException extends Exception {

  public QueryException() {
    super("Error during contract query");
  }

  public QueryException(String message) {
    super(message);
  }

  public QueryException(String message, Exception e) {
    super(message, e);
  }

  public QueryException(Exception e) {
    super("Error during contract query", e);
  }

}
