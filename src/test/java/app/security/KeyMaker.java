package app.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

final class KeyMaker {
  private KeyMaker() {}

  private static final Key SECRET = Keys.secretKeyFor(SignatureAlgorithm.HS256);

  static String jwtWithRoles(List<String> roles) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("roles", roles);
    claims.put("nam", "user");
    claims.put("ema", "user@test.com");
    return Jwts.builder()
      .setClaims(claims)
      .signWith(SECRET)
      .setSubject("1")
      .compact();
  }

  static KongToken kongTokenWithRoles(List<String> roles) {
    String token = "Bearer " + jwtWithRoles(roles);
    return TokenParser.parse(token);
  }

  static void loginWithRoles(List<String> roles) {
    Authentication kongAuth = kongTokenWithRoles(roles);
    SecurityContextHolder.getContext().setAuthentication(kongAuth);
  }

  static void logout() {
    SecurityContextHolder.getContext().setAuthentication(null);
  }
}
