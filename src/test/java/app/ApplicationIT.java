package app;

import app.annotations.IntegrationTest;
import org.junit.jupiter.api.Test;

@IntegrationTest
class ApplicationIT {
  @Test
  void testContextLoads() { //NOSONAR
    // ONLY context loaded
  }
}
