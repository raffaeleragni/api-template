package app.json;

import static app.json.ObjectMapperForTesting.getObjectMapperForTesting;
import app.json.TestRecord;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Optional;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.skyscreamer.jsonassert.JSONAssert;

class JacksonTest {
  ObjectMapper mapper;

  @BeforeEach
  void setup() {
    mapper = getObjectMapperForTesting();
  }

  @Test
  void testJacksonDates() throws IOException {
    var date = "\"2017-05-23T12:10:19Z\"";

    var datetime = mapper.readValue(date, ZonedDateTime.class);
    var date2 = mapper.writeValueAsString(datetime);

    assertThat("Dates serialize", date, is(date2));
  }

  @Test
  void testRecords() throws JSONException, JsonProcessingException {
    var originalRec = new TestRecord("test", Optional.of(1));
    var json = mapper.writeValueAsString(originalRec);
    var readRec = mapper.readValue(json, TestRecord.class);

    assertThat(readRec, is(originalRec));

    JSONAssert.assertEquals(
    """
    {"a": "test", "b": 1}
    """, json, true);

    var jsonNoB = mapper.writeValueAsString(new TestRecord("test", Optional.empty()));
    System.out.println(jsonNoB);
    JSONAssert.assertEquals(
    """
    {"a": "test"}
    """, jsonNoB, true);
  }

}

record TestRecord(String a, Optional<Integer> b) {}
