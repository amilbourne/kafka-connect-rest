package com.tm.kafka.connect.rest.http.payload.templated;


import com.jayway.jsonpath.*;
import com.tm.kafka.connect.rest.http.Request;
import com.tm.kafka.connect.rest.http.Response;
import org.apache.kafka.common.Configurable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * Lookup values used to populate dynamic payloads.
 * These values will be substituted into the payload template.
 *
 * This implementation uses JSONPath to extract values from a JSON HTTP response,
 * and if not found looks them up in the System properties and then in environment variables.
 */
public class JsonPathResponseValueProvider extends EnvironmentValueProvider implements Configurable {

  private static Logger log = LoggerFactory.getLogger(JsonPathResponseValueProvider.class);
  private static final Configuration JSONPATH_CONFIGURATION = Configuration.defaultConfiguration().setOptions(Option.ALWAYS_RETURN_LIST);
  private static final String MULTI_VALUE_SEPARATOR = ",";

  private Map<String, JsonPath> expressions;


  /**
   * Configure this instance after creation.
   *
   * @param props The configuration properties
   */
  @Override
  public void configure(Map<String, ?> props) {
    final JsonPathResponseValueProviderConfig config = new JsonPathResponseValueProviderConfig(props);
    setExpressions(config.getResponseVariableJsonPaths());
  }

  /**
   * Extract values from the response using the JSONPaths
   *
   * @param request The last request made.
   * @param response The last response received.
   */
  @Override
  protected void extractValues(Request request, Response response) {
    String resp = response.getPayload();
    try {
      Object respDoc = Configuration.defaultConfiguration().jsonProvider().parse(resp);
      expressions.forEach((key, expr) -> parameterMap.put(key, extractValue(key, respDoc, expr)));
    } catch (InvalidJsonException ex) {
      log.error("The JSON could not be parsed: " + resp, ex);
    }
  }

  /**
   * Set the JSONPaths to be used for value extraction.
   *
   * @param jsonPaths A map of key names to JSONPath expressions
   */
  protected void setExpressions(Map<String, String> jsonPaths) {
    expressions = new HashMap<>(jsonPaths.size());
    parameterMap = new HashMap<>(expressions.size());
    jsonPaths.forEach(this::addJsonPath);
  }

  /**
   * Extract the value for a given key.
   * Where the JSONPath yeilds more than one result a comma seperated list will be returned.
   *
   * @param key The name of the key
   * @param respDom The response to extract a value from
   * @param expression The compiled JSONPath used to find the value
   * @return Return the value, or null if it wasn't found
   */
  private String extractValue(String key, Object respDom, JsonPath expression) {
    try {
      List<Object> values = expression.read(respDom, JSONPATH_CONFIGURATION);
      String value = (values.size() != 0) ?
        values.stream().map(Object::toString).collect(Collectors.joining(MULTI_VALUE_SEPARATOR)) : null;
      log.info("Variable {} was assigned the value {}", key, value);
      return value;
    } catch (PathNotFoundException ex) {
      // This isn't an error - so just return null.
      return null;
    } catch (RuntimeException ex) {
      log.error("The JSONPath expression '" + expression + "' could not be evaluated against: " + respDom, ex);
      return null;
    }
  }

  private void addJsonPath(String key, String jsonPath) {
    try {
      expressions.put(key, JsonPath.compile(jsonPath));
    } catch (InvalidPathException ex) {
      log.error("The JSONPath expression '" + jsonPath + "' could not be compiled", ex);
    }
  }
}
