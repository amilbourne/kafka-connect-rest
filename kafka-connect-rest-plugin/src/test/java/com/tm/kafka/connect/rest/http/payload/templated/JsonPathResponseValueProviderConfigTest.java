package com.tm.kafka.connect.rest.http.payload.templated;


import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.collection.IsMapContaining.hasEntry;
import static org.junit.Assert.assertThat;


public class JsonPathResponseValueProviderConfigTest {

  @Test
  public void testConfig() {
    Map<String, Object> props = new HashMap<>();

    props.put("rest.source.response.var.names", "key1, key2");
    props.put("rest.source.response.var.key1.jsonpath", "$['results'][*]");
    props.put("rest.source.response.var.key2.jsonpath", "$['response-code']");

    JsonPathResponseValueProviderConfig config = new JsonPathResponseValueProviderConfig(props);

    assertThat(config.getResponseVariableNames(), contains("key1", "key2"));
    assertThat(config.getResponseVariableJsonPaths(), allOf(
      hasEntry("key1", "$['results'][*]"),
      hasEntry("key2", "$['response-code']")));
  }
}
