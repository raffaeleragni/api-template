package app.security;

import app.api.SampleController;
import static app.security.LoginHelper.makeTokenFromRoles;
import java.util.Arrays;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import org.hamcrest.collection.IsEmptyCollection;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class AuthenticationIT {

  @Autowired SampleController controller;

  @Test
  void verifyNoRoleLogin() {
    var auth = LoginHelper.makeTokenFromRoles(Arrays.asList("api.norole"));
    assertThat("No role taken", auth.getAuthorities(), IsEmptyCollection.empty());

    auth = LoginHelper.makeTokenFromRoles(Arrays.asList("api"));
    assertThat("No role taken", auth.getAuthorities(), IsEmptyCollection.empty());

    auth = LoginHelper.makeTokenFromRoles(Arrays.asList("arole"));
    assertThat("No role taken", auth.getAuthorities(), IsEmptyCollection.empty());
  }

  @Test
  void verifyJWTFlowBad() {
    LoginHelper.logout();
    var ex = assertThrows(AuthenticationCredentialsNotFoundException.class, () -> {
      var result = controller.test();
    });
    assertThat("Not found exception message", ex.getMessage(), containsString("not found"));
  }

  @Test
  void verifyJWTFlowGood() {
    LoginHelper.loginWithRoles(Arrays.asList("api.user"));
    assertThat("Test returned test", controller.test(), is("test"));
  }

  @Test
  void verifyKongLayer() {
    var kongAuth = makeTokenFromRoles(Arrays.asList("api.user"));
    assertThat("credentials not implemented", kongAuth.getCredentials(), is(nullValue()));
    assertThat("details not implemented", kongAuth.getDetails(), is(nullValue()));
  }
}
