package app.health;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component
class Version implements HealthIndicator {

  @Value("${spring.application.version}")
  private String applicationVersion;

  @Override
  public Health health() {
    return Health.up().withDetail("version", applicationVersion).build();
  }
}
