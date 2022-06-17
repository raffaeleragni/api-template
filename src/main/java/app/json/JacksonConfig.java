package app.json;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_ABSENT;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import static com.fasterxml.jackson.databind.MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS;
import com.fasterxml.jackson.databind.ObjectMapper;
import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SNAKE_CASE;
import com.fasterxml.jackson.databind.SerializationFeature;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import static java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
class JacksonConfig {

  @Bean
  @Primary
  ObjectMapper objectMapper() {
    var mapper = JsonMapper.builder()
      .enable(INDENT_OUTPUT)
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
      .configure(READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
      .configure(ACCEPT_CASE_INSENSITIVE_ENUMS, true)
      .propertyNamingStrategy(SNAKE_CASE)
      .serializationInclusion(NON_ABSENT)
      .build();

    mapper.registerModule(new JavaTimeModule());
    mapper.registerModule(new Jdk8Module());
    mapper.setVisibility(
      mapper.getSerializationConfig()
        .getDefaultVisibilityChecker()
        .withFieldVisibility(ANY)
        .withGetterVisibility(ANY)
        .withSetterVisibility(ANY)
        .withCreatorVisibility(ANY)
    );

    var module = new SimpleModule();
    DateTimeFormatter formatterWrite = ISO_OFFSET_DATE_TIME;
    DateTimeFormatter fromatterRead = ISO_OFFSET_DATE_TIME;
    module.addSerializer(ZonedDateTime.class, new JsonSerializer<ZonedDateTime>() {
      @Override
      public void serialize(ZonedDateTime zonedDateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeString(formatterWrite.format(zonedDateTime));
      }
    });
    module.addDeserializer(ZonedDateTime.class, new JsonDeserializer<ZonedDateTime>() {
      @Override
      public ZonedDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return ZonedDateTime.from(fromatterRead.parse(p.getText()));
      }
    });
    mapper.registerModule(module);

    return mapper;
  }
}
