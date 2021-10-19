package com.blkchainsolutions.fabric.client;

public interface FabricClient extends 
  Invoke, Query {

  /**
   * shutdown the application
   * @param force force shutdown, note that it does not work with gateway java impl
   */
  public void shutdown(boolean force);
}
