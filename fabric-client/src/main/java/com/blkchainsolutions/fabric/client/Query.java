package com.blkchainsolutions.fabric.client;

import com.blkchainsolutions.fabric.client.exception.QueryException;
import com.blkchainsolutions.fabric.client.exception.SerializationException;
import com.blkchainsolutions.fabric.client.param.QueryInvokeArgs;
import com.blkchainsolutions.fabric.identity.exception.IdentityNotFoundException;

public interface Query {

  /**
   * dynamic query fabric (must support couchdb)
   * @param args the arguments, including the function name and dynamic query string
   * @return payload from fabric as string
   * @throws QueryException
   * @throws IdentityNotFoundException
   */
  String query(final QueryInvokeArgs args) throws QueryException, IdentityNotFoundException;
  
  /**
   * dynamic query fabric (must support couchdb)
   * @param <T> the returned type
   * @param args the arguments, including the function name and dynamic query string
   * @param payloadMapper a mapper callback to transform payload string to an object (most likely a json mapper)
   * @return payload transformed as the desginated type
   * @throws QueryException
   * @throws SerializationException
   * @throws IdentityNotFoundException
   */
  <T> T query(final QueryInvokeArgs args, FabricPayloadMapper<T> payloadMapper) throws SerializationException, QueryException, IdentityNotFoundException;
  
}
