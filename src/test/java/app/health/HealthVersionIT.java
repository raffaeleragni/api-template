package app.health;

import app.json.ObjectMapperHelper;
import com.fasterxml.jackson.databind.JsonNode;
import java.net.URI;
import java.net.URISyntaxException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.actuate.metrics.AutoConfigureMetrics;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import org.springframework.boot.test.web.server.LocalServerPort;

@AutoConfigureMetrics
@SpringBootTest(webEnvironment = RANDOM_PORT)
@ActiveProfiles("test")
class HealthVersionIT {

  @LocalServerPort int randomServerPort;

  @Test
  void testVersion() throws Exception {
    RestTemplate restTemplate = new RestTemplate();
    URI uri = new URI("http://localhost:" + randomServerPort + "/health/check");

    ResponseEntity<String> response = restTemplate.getForEntity(uri, String.class);
    var json = ObjectMapperHelper.getObjectMapper().readValue(response.getBody(), JsonNode.class);

    var version = json.get("components").get("version").get("details").get("version").asText();
    assertThat("Version returned is the one defined in test profile", version, is("test-version"));

    var lb = new URI("http://localhost:" + randomServerPort + "/health/check/lb");
    assertThat(restTemplate.getForEntity(lb, String.class).getStatusCode(), is(HttpStatus.OK));
  }

  @Test
  void testInaccessibleProtectedEndpoints() throws URISyntaxException {
    RestTemplate restTemplate = new RestTemplate();
    var baseurl = "http://localhost:" + randomServerPort;
    var uriheap = new URI(baseurl + "/heapdump");
    var urienv = new URI(baseurl + "/env");
    var prometheus = new URI(baseurl + "/prometheus");

    assertThrows(HttpClientErrorException.NotFound.class, () -> restTemplate.getForEntity(uriheap, String.class));
    assertThrows(HttpClientErrorException.NotFound.class, () -> restTemplate.getForEntity(urienv, String.class));

    assertThat(restTemplate.getForEntity(prometheus, String.class).getStatusCode(), is(HttpStatus.OK));
  }
}
