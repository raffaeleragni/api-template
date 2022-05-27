package app.security;

import io.jsonwebtoken.*;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.springframework.security.authentication.InsufficientAuthenticationException;

public final class TokenParser {

  private TokenParser() { }

  private static final String AUTHORIZATION_SCHEMA = "Bearer";
  private static final String CLAIM_EMAIL = "ema";
  private static final String CLAIM_ROLES = "roles";

  public static List<Role> extractRoles(Claims claims) {
    var stringRoles = (List<String>) claims.get(CLAIM_ROLES);

    return stringRoles.stream()
      .map(s -> Role.of(s).orElse(null))
      .filter(Objects::nonNull)
      .toList();
  }

  public static KongToken parse(String authorizationString) {
    if (authorizationString == null || authorizationString.isEmpty())
      throw new InsufficientAuthenticationException("Token required for API");
    if (!authorizationString.startsWith(AUTHORIZATION_SCHEMA))
      throw new InsufficientAuthenticationException("Authorization schema not found");

    var token = stripSignature(stripSchema(authorizationString));

    try {
      var jwt = Jwts.parserBuilder().build().parseClaimsJwt(token);
      var tokenBody = jwt.getBody();
      var principal = subject(tokenBody);
      var userName = email(tokenBody).orElse(String.valueOf(principal));
      var authorities = extractRoles(tokenBody);

      return new KongToken(principal, userName, authorities);
    } catch (JwtException e) {
      throw new InsufficientAuthenticationException("Unable to parse token: " + e.getMessage());
    }
  }

  private static Optional<String> email(Claims claims) {
    return Optional.ofNullable((String) claims.get(CLAIM_EMAIL));
  }

  private static String stripSignature(String withoutSchema) {
    return withoutSchema.substring(0, withoutSchema.lastIndexOf('.') + 1);
  }

  private static String stripSchema(String authorizationString) {
    return authorizationString.substring(AUTHORIZATION_SCHEMA.length()).trim();
  }

  private static String subject(Claims claims) {
    return Objects.requireNonNull(claims.getSubject());
  }
}
