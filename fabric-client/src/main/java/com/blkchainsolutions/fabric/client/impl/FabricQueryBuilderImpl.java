package com.blkchainsolutions.fabric.client.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import com.blkchainsolutions.fabric.client.FabricQueryBuilder;
import com.blkchainsolutions.fabric.client.exceptions.SerializationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class FabricQueryBuilderImpl implements FabricQueryBuilder {
  private FabricQueryBuilderImpl that;
  private HashSet<String> keys;
  private LinkedList<String> reverseOrderedKeys;
  private HashMap<String, Object> mappings;

  public FabricQueryBuilderImpl key(final String fieldName) {
    if (keys.contains(fieldName)) 
      throw new IllegalArgumentException("field is already set");

    keys.add(fieldName);
    reverseOrderedKeys.addFirst(fieldName);
    return this;
  }

  public FabricQueryBuilderImpl regex() {
    return key("$regex");
  } 

  public FabricQueryBuilderImpl mod() {
    return key("$mod");
  } 

  public FabricQueryBuilderImpl in() {
    return key("$in");
  } 

  public FabricQueryBuilderImpl notIn() {
    return key("$nin");
  } 

  public FabricQueryBuilderImpl not() {
    return key("$not");
  }

  public FabricQueryBuilderImpl or() {
    return key("$or");
  } 

  public FabricQueryBuilderImpl and() {
    return key("$and");
  } 

  public FabricQueryBuilderImpl nor() {
    return key("$nor");
  } 

  public FabricQueryBuilderImpl elemMatch() {
    return key("$elemMatch");
  } 

  public FabricQueryBuilderImpl allMatch() {
    return key("$allMatch");
  } 

  public FabricQueryBuilderImpl keyMapMatch() {
    return key("$keyMapMatch");
  } 

  public FabricQueryBuilderImpl greaterThan() {
    return key("$gt");
  } 

  public FabricQueryBuilderImpl greaterThanEqual() {
    return key("$gte");
  } 

  public FabricQueryBuilderImpl lessThan() {
    return key("$lt");
  } 

  public FabricQueryBuilderImpl lessThanEqual() {
    return key("$lte");
  } 

  public FabricQueryBuilderImpl equal() {
    return key("$eq");
  } 

  public FabricQueryBuilderImpl notEqual() {
    return key("$ne");
  } 

  public FabricQueryBuilderImpl id() {
    return key("_id");
  }

  public FabricQueryBuilderImpl selector() {
    FabricQueryBuilderImpl query = key("selector");
    return query.subquery();
  }

  @Override
  public FabricQueryBuilder bookmark(String bookmark) {
    FabricQueryBuilderImpl query = key("bookmark");
    return query.expect(bookmark);
  }

  @Override
  public FabricQueryBuilder pageSize(int pageSize) {
    FabricQueryBuilderImpl query = key("pageSize");
    return query.expect(pageSize);
  }

  public FabricQueryBuilderImpl fields(List<String> fields) {
    FabricQueryBuilderImpl query = key("fields");
    return query.expect(fields);
  }

  public FabricQueryBuilderImpl sort() {
    return key("sort");
  }

  public FabricQueryBuilderImpl sort(List<String> sortedByFields) {
    FabricQueryBuilderImpl query = key("sort");
    return query.expect(sortedByFields);
  }

  public FabricQueryBuilderImpl useIndex(List<String> idxes) {
    FabricQueryBuilderImpl query = key("use_index");
    return query.expect(idxes);
  }

  private FabricQueryBuilderImpl expect(final Object expected) {
    final String key = reverseOrderedKeys.getFirst();
    if (null == key) 
      throw new IllegalArgumentException("field is not set");
    if (mappings.containsKey(key)) 
      throw new IllegalArgumentException("value is set for this field");

    mappings.put(key, expected);
    return this;
  }

  public FabricQueryBuilderImpl expect(final String expected) {
    final String key = reverseOrderedKeys.getFirst();
    if (null == key) 
      throw new IllegalArgumentException("field is not set");
    if (mappings.containsKey(key)) 
      throw new IllegalArgumentException("value is set for this field");

    mappings.put(key, expected);
    return this;
  }

  public FabricQueryBuilderImpl expectReplacement() {
    return expect("?");
  }

  public FabricQueryBuilderImpl subquery() {
    final String key = reverseOrderedKeys.getFirst();
    if (null == key) 
      throw new IllegalArgumentException("field is not set");
    if (mappings.containsKey(key)) 
      throw new IllegalArgumentException("value is set for this field");

    FabricQueryBuilderImpl newBuilder = new FabricQueryBuilderImpl();
    newBuilder.that = this;
    mappings.put(key, newBuilder);

    return newBuilder;
  }

  public FabricQueryBuilderImpl list() {
    final String key = reverseOrderedKeys.getFirst();
    if (null == key) 
      throw new IllegalArgumentException("field is not set");
    if (mappings.containsKey(key)) 
      throw new IllegalArgumentException("value is set for this field");

    List<Object> li = new ArrayList<>();
    mappings.put(key, li);
    return this;
  }

  public FabricQueryBuilderImpl addSubquery() {
    final String key = reverseOrderedKeys.getFirst();
    if (null == key) 
      throw new IllegalArgumentException("field is not set");
    if (!mappings.containsKey(key)) 
      throw new IllegalArgumentException("value is not set");
    Object value = mappings.get(key);
    if (!(value instanceof List))
      throw new IllegalArgumentException("list is not set for this field");

    List<Object> li = (List<Object>) value;

    FabricQueryBuilderImpl newBuilder = new FabricQueryBuilderImpl();
    li.add(newBuilder);
    newBuilder.that = this;

    return newBuilder;
  }

  private FabricQueryBuilderImpl addObject(Object obj) {
    final String key = reverseOrderedKeys.getFirst();
    if (null == key) 
      throw new IllegalArgumentException("field is not set");
    if (!mappings.containsKey(key)) 
      throw new IllegalArgumentException("value is not set");
    Object value = mappings.get(key);
    if (!(value instanceof List))
      throw new IllegalArgumentException("list is not set for this field");

    List<Object> li = (List<Object>) value;

    li.add(obj);

    return this;
  }

  public FabricQueryBuilderImpl addString(String str) {
    return addObject(str);
  }

  public FabricQueryBuilderImpl end() {
    if (null == this.that) 
      throw new IllegalArgumentException("this query is not nested");

    return this.that;
  }

  public String buildQueryString() throws SerializationException {
    ObjectMapper mapper = new ObjectMapper();
    SimpleModule module = new SimpleModule();
    module.addSerializer(FabricQueryBuilderImpl.class, new FabricQueryBuilderSerializer());
    mapper.registerModule(module);

    try {
      return mapper.writeValueAsString(this);
    } catch (Exception e) {
      throw new SerializationException("Error building query string", e);
    }
  }

  class FabricQueryBuilderSerializer extends StdSerializer<FabricQueryBuilderImpl> {

    public FabricQueryBuilderSerializer() {
      super(FabricQueryBuilderImpl.class, true);
    }

    @Override
    public void serialize(FabricQueryBuilderImpl value, JsonGenerator jgen, SerializerProvider provider)
        throws IOException, JsonProcessingException {
      jgen.writeObject(value.mappings);;
    }

  }

}
