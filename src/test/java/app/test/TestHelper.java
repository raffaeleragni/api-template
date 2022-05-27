package app.test;

import app.json.JacksonConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

public class TestHelper {
  public static ObjectMapper getObjectMapper() {
    return new JacksonConfig().objectMapper();
  }
}
