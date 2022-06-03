package app.affinity;

import app.affinity.Affinity.Values;
import java.util.HashMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.core.type.AnnotatedTypeMetadata;

@ExtendWith(MockitoExtension.class)
class AffinityTest {
  @InjectMocks AffinitySelector selector;
  @Mock ConditionContext context;
  @Mock Environment environment;
  @Mock AnnotatedTypeMetadata metadata;

  @ParameterizedTest
  @CsvSource(nullValues = {"null"}, value = {
    "null,null,true",
    "NONE,null,false",
    "QUEUE,queue,true",
    "API,api,true",
    "API,queue,false",
    "QUEUE,api,false"
  })
  void testNoAffinity(
    Affinity.Values affinity,
    String acceptedProfile,
    boolean enabled) {

    setupAffinity(affinity);
    setupProfile(acceptedProfile);
    var result = selector.matches(context, metadata);
    assertThat(result, is(enabled));
  }

  private void setupAffinity(Values affinity) {
    var map = affinity == null ? null : affinityMapFor(affinity);
    when(metadata.getAnnotationAttributes(Affinity.class.getName()))
            .thenReturn(map);
  }

  private HashMap<String, Object> affinityMapFor(Values affinity) {
    var map = new HashMap<String, Object>();
    map.put("value", affinity);
    return map;
  }

  private void setupProfile(String profile) {
    if (profile == null)
      return;
    when(context.getEnvironment()).thenReturn(environment);
    when(environment.acceptsProfiles(Profiles.of(profile)))
            .thenReturn(true);
  }
}
