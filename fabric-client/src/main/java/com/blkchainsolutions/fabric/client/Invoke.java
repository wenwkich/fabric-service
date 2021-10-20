package com.blkchainsolutions.fabric.client;

import com.blkchainsolutions.fabric.client.exception.InvokeException;
import com.blkchainsolutions.fabric.client.exception.SerializationException;
import com.blkchainsolutions.fabric.client.param.QueryInvokeArgs;
import com.blkchainsolutions.fabric.identity.exception.IdentityNotFoundException;

public interface Invoke {

  /**
   * invoke fabric
   * @param args invoke arguments, including function name and arguments
   * @return payload from fabric as string
   * @throws InvokeException
   * @throws IdentityNotFoundException
   */
  String invoke(final QueryInvokeArgs args) throws InvokeException, IdentityNotFoundException;

  /**
   * invoke fabric
   * @param <T> the returned type
   * @param args invoke arguments, including function name and arguments
   * @param payloadMapper a mapper callback to transform payload string to an object (most likely a json mapper)
   * @return payload transformed as the desginated type
   * @throws SerializationException
   * @throws IdentityNotFoundException
   */
  <T> T invoke(final QueryInvokeArgs args, FabricPayloadMapper<T> payloadMapper) throws InvokeException, SerializationException, IdentityNotFoundException;
  
}
