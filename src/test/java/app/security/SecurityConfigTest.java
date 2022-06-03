package app.security;

import static app.security.Role.USER;
import java.util.Arrays;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

  private static final String USER_JWT = KeyMaker.jwtWithRoles(Arrays.asList("api.user"));

  @InjectMocks KongFilter filter;
  @Mock HttpServletRequest req;
  @Mock HttpServletResponse resp;
  @Mock FilterChain chain;

  @Test
  void testNoToken() throws Exception {
    filter.doFilter(req, resp, chain);
    assertThat("No auth was given", SecurityContextHolder.getContext().getAuthentication(), is(nullValue()));
  }

  @Test
  void testValidUserToken() throws Exception {
    setupJWT(USER_JWT);
    filter.doFilter(req, resp, chain);
    verifyToken(USER, "1", "user@test.com");
  }

  private void verifyToken(Role role, String subject, String email) throws Exception {
    var auth = SecurityContextHolder.getContext().getAuthentication();
    verify(chain).doFilter(req, resp);
    assertThat("Role taken", auth.getAuthorities(), contains(role));
    assertThat("Subject taken", auth.getPrincipal(), is(subject));
    assertThat("Email taken", auth.getName(), is(email));
  }

  @Test
  void testBadHeaderFormats() {
    assertThrows(InsufficientAuthenticationException.class, () -> {
      TokenParser.parse("Bearer Token");
    });
    assertThrows(InsufficientAuthenticationException.class, () -> {
      TokenParser.parse("Beaver TXCE");
    });
    assertThrows(InsufficientAuthenticationException.class, () -> {
      TokenParser.parse(null);
    });
    assertThrows(InsufficientAuthenticationException.class, () -> {
      TokenParser.parse("");
    });
    assertThrows(InsufficientAuthenticationException.class, () -> {
      TokenParser.parse("");
    });
  }

  private void setupJWT(String token) {
    when(req.getHeader("Authorization")).thenReturn("Bearer " + token);
  }
}
