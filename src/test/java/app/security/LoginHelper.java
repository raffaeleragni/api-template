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

final class LoginHelper {

  private LoginHelper() {}

  private static final Key SIGNING_KEY = Keys.secretKeyFor(SignatureAlgorithm.HS256);

  static String makeEncodedTokenFromRoles(List<String> roles) {
    Map<String, Object> claims = new HashMap<>();
    claims.put("roles", roles);
    claims.put("nam", "user");
    claims.put("ema", "user@test.com");
    return Jwts.builder()
      .setClaims(claims)
      .signWith(SIGNING_KEY)
      .setSubject("1")
      .compact();
  }

  static KongToken makeTokenFromRoles(List<String> roles) {
    String token = "Bearer " + makeEncodedTokenFromRoles(roles);
    return TokenParser.parse(token);
  }

  static void loginWithRoles(List<String> roles) {
    Authentication kongAuth = makeTokenFromRoles(roles);
    SecurityContextHolder.getContext().setAuthentication(kongAuth);
  }

  static void logout() {
    SecurityContextHolder.getContext().setAuthentication(null);
  }
}
