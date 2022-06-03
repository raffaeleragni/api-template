package app.security;

import io.micrometer.core.instrument.util.StringUtils;
import java.util.Optional;
import static java.util.Optional.empty;
import org.springframework.security.core.GrantedAuthority;

enum Role implements GrantedAuthority { //NOSONAR

  USER;

  private static final String SPRING_SECURITY_PREFIX = "ROLE_";

  @Override
  public String getAuthority() {
    return SPRING_SECURITY_PREFIX + toString();
  }

  private static final String API_PREFIX = "API";

  public static Optional<Role> of(String stringRole) {
    return Optional.ofNullable(stringRole)
      .filter(StringUtils::isNotBlank)
      .map(String::toUpperCase)
      .filter(Role::isApplicationRole)
      .flatMap(Role::toFirstRoleOrEmpty);
  }

  private static boolean isApplicationRole(String string) {
    return string.startsWith(API_PREFIX);
  }

  private static Optional<Role> toFirstRoleOrEmpty(String strRole) {
    for (Role r: values())
      if (strRole.equals(API_PREFIX+"."+r.name()))
        return Optional.of(r);
    return empty();
  }
}
