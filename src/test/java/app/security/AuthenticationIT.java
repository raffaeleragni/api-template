package app.security;

import app.test.annotations.IT;
import java.util.Arrays;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import org.hamcrest.collection.IsEmptyCollection;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import static app.security.KeyMaker.kongTokenWithRoles;

@IT
class AuthenticationIT {

  @Autowired SampleController controller;

  @Test
  void verifyNoRoleLogin() {
    var auth = KeyMaker.kongTokenWithRoles(Arrays.asList("api.norole"));
    assertThat("No role taken", auth.getAuthorities(), IsEmptyCollection.empty());

    auth = KeyMaker.kongTokenWithRoles(Arrays.asList("api"));
    assertThat("No role taken", auth.getAuthorities(), IsEmptyCollection.empty());

    auth = KeyMaker.kongTokenWithRoles(Arrays.asList("arole"));
    assertThat("No role taken", auth.getAuthorities(), IsEmptyCollection.empty());
  }

  @Test
  void verifyJWTFlowBad() {
    KeyMaker.logout();
    var ex = assertThrows(AuthenticationCredentialsNotFoundException.class, () -> controller.test());
    assertThat("Not found exception message", ex.getMessage(), containsString("not found"));
  }

  @Test
  void verifyJWTFlowGood() {
    KeyMaker.loginWithRoles(Arrays.asList("api.user"));
    assertThat("Test returned test", controller.test(), is("test"));
  }

  @Test
  void verifyKongLayer() {
    var kongAuth = kongTokenWithRoles(Arrays.asList("api.user"));
    assertThat("credentials not implemented", kongAuth.getCredentials(), is(nullValue()));
    assertThat("details not implemented", kongAuth.getDetails(), is(nullValue()));
  }
}

@RestController
class SampleController {
  @PreAuthorize("hasRole('USER')") //NOSONAR
  @GetMapping("/test")
  String test() {
    return "test";
  }
}
