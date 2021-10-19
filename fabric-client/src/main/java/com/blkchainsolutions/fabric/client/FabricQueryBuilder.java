package com.blkchainsolutions.fabric.client;

import java.util.List;

import com.blkchainsolutions.fabric.client.exceptions.SerializationException;

public interface FabricQueryBuilder {
  FabricQueryBuilder key(final String fieldName);

  FabricQueryBuilder regex();

  FabricQueryBuilder mod();

  FabricQueryBuilder in();

  FabricQueryBuilder notIn();

  FabricQueryBuilder not();

  FabricQueryBuilder or();

  FabricQueryBuilder and();

  FabricQueryBuilder nor();

  FabricQueryBuilder elemMatch();

  FabricQueryBuilder allMatch();

  FabricQueryBuilder keyMapMatch();

  FabricQueryBuilder greaterThan();

  FabricQueryBuilder greaterThanEqual();

  FabricQueryBuilder lessThan();

  FabricQueryBuilder lessThanEqual();

  FabricQueryBuilder equal();

  FabricQueryBuilder notEqual();

  FabricQueryBuilder id();

  FabricQueryBuilder selector();

  FabricQueryBuilder bookmark(final String bookmark);

  FabricQueryBuilder pageSize(final int pageSize);

  FabricQueryBuilder fields(final List<String> fields);

  FabricQueryBuilder sort();

  FabricQueryBuilder sort(final List<String> sortedByFields);

  FabricQueryBuilder useIndex(final List<String> idxes);

  FabricQueryBuilder expect(final String expected);

  FabricQueryBuilder subquery();

  FabricQueryBuilder list();

  FabricQueryBuilder addSubquery();

  FabricQueryBuilder addString(final String string);

  FabricQueryBuilder end();

  String buildQueryString() throws SerializationException;
}
