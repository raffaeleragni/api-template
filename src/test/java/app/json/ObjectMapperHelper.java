package app.json;

import com.fasterxml.jackson.databind.ObjectMapper;

public class ObjectMapperHelper {
  public static ObjectMapper getObjectMapper() {
    return new JacksonConfig().objectMapper();
  }
}
