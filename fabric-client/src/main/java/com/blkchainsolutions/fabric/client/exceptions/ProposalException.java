package com.blkchainsolutions.fabric.client.exceptions;

public class ProposalException extends Exception {
  
  public ProposalException() {
    super("Error during proposal phase");
  }

  public ProposalException(String message) {
    super(message);
  }

  public ProposalException(String message, Exception e) {
    super(message, e);
  }

  public ProposalException(Exception e) {
    super("Error during proposal phase", e);
  }

}
