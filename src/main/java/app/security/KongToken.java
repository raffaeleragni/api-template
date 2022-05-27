package app.security;

import java.util.Collection;
import lombok.Data;
import org.springframework.security.core.Authentication;

@Data
public class KongToken implements Authentication {

  private static final long serialVersionUID = 1L;

  private final String principal;
  private final String name;
  private final Collection<Role> authorities;
  private boolean isAuthenticated = true;

  KongToken(String principal, String name, Collection<Role> authorities) {
    this.principal = principal;
    this.name = name;
    this.authorities = authorities;
  }

  @Override
  public Object getCredentials() {
    return null;
  }

  @Override
  public Object getDetails() {
    return null;
  }
}
