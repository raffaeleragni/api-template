package app.json;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class ObjectMapperForTesting {
  private ObjectMapperForTesting() {}

  public static ObjectMapper getObjectMapperForTesting() {
    return new JacksonConfig().objectMapper();
  }
}
