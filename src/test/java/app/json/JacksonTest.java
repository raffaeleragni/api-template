package app.json;

import static app.json.ObjectMapperForTesting.getObjectMapperForTesting;
import java.io.IOException;
import java.time.ZonedDateTime;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.Test;

class JacksonTest {

  @Test
  void testJacksonDates() throws IOException {
    var mapper = getObjectMapperForTesting();

    var date = "\"2017-05-23T12:10:19Z\"";

    var datetime = mapper.readValue(date, ZonedDateTime.class);
    var date2 = mapper.writeValueAsString(datetime);

    assertThat("Dates serialize", date, is(date2));
  }

}
