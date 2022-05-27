package app.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.web.filter.GenericFilterBean;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
class SecurityConfig {
  @Bean SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    return http
      .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
      .and()
      // .csrf().disable() //NOSONAR
      .authorizeRequests() //NOSONAR
      .antMatchers("/health/**").permitAll()
      .anyRequest().permitAll()
      .and()
      .addFilterBefore(new KongFilter(), AbstractPreAuthenticatedProcessingFilter.class)
      .build();
  }
}

class KongFilter extends GenericFilterBean {
  @Override
  public void doFilter(ServletRequest req, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
    var request = (HttpServletRequest) req;
    try {
      var authentication = TokenParser.parse(request.getHeader("Authorization"));
      SecurityContextHolder.getContext().setAuthentication(authentication);
      filterChain.doFilter(request, response);
    } catch (AuthenticationException e) {
      SecurityContextHolder.clearContext();
      filterChain.doFilter(request, response);
    }
  }
}

final class TokenParser {

  private TokenParser() { }

  private static final String AUTHORIZATION_SCHEMA = "Bearer";
  private static final String CLAIM_EMAIL = "ema";
  private static final String CLAIM_ROLES = "roles";

  static List<Role> extractRoles(Claims claims) {
    var stringRoles = (List<String>) claims.get(CLAIM_ROLES);

    return stringRoles.stream()
      .map(s -> Role.of(s).orElse(null))
      .filter(Objects::nonNull)
      .toList();
  }

  static KongToken parse(String authorizationString) {
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
