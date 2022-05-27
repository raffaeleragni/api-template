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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;

class KongFilterTest {

  @Test
  void test() throws Exception {
    var filter = new KongFilter();
    var req = mock(HttpServletRequest.class);
    var resp = mock(HttpServletResponse.class);
    var chain = mock(FilterChain.class);

    when(req.getHeader("Authorization"))
      .thenReturn("Bearer " + LoginHelper.makeEncodedTokenFromRoles(Arrays.asList("api.user")));

    filter.doFilter(req, resp, chain);

    var auth = SecurityContextHolder.getContext().getAuthentication();
    verify(chain).doFilter(req, resp);
    assertThat("Role taken", auth.getAuthorities(), contains(USER));
    assertThat("Subject taken", auth.getPrincipal(), is("1"));
    assertThat("Email taken", auth.getName(), is("user@test.com"));
  }

  @Test
  void testBadToken() throws Exception {

    var filter = new KongFilter();
    var req = mock(HttpServletRequest.class);
    var resp = mock(HttpServletResponse.class);
    var chain = mock(FilterChain.class);

    filter.doFilter(req, resp, chain);
    var auth = SecurityContextHolder.getContext().getAuthentication();
    assertThat("No auth was given", auth, is(nullValue()));
  }

  @Test
  void testBadFormats() {
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
}
