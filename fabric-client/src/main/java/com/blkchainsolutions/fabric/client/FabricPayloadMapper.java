package com.blkchainsolutions.fabric.client;

import com.blkchainsolutions.fabric.client.exceptions.SerializationException;

public interface FabricPayloadMapper<T> {
  public T mapPayload(String json) throws SerializationException;
}
