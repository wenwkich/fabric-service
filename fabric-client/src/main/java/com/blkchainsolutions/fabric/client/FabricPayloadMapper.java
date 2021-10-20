package com.blkchainsolutions.fabric.client;

import com.blkchainsolutions.fabric.client.exception.SerializationException;

public interface FabricPayloadMapper<T> {
  public T mapPayload(String json) throws SerializationException;
}
