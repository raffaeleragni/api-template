package app.affinity;

import static app.affinity.Affinity.Values.API;
import static app.affinity.Affinity.Values.QUEUE;
import java.util.HashMap;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.NONE;
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

  void testNoAffinity() {
    when(metadata.getAnnotationAttributes(Affinity.class.getName()))
      .thenReturn(null);
    var result = selector.matches(context, metadata);

    assertThat(result, is(true));
  }

  @Test
  void testQueueAffinity() {
    var map = new HashMap<String, Object>();
    map.put("value", QUEUE);

    when(metadata.getAnnotationAttributes(Affinity.class.getName()))
      .thenReturn(map);
    when(context.getEnvironment()).thenReturn(environment);
    when(environment.acceptsProfiles(Profiles.of("queue")))
      .thenReturn(true);

    var result = selector.matches(context, metadata);

    assertThat(result, is(true));
  }

  @Test
  void testApiAffinity() {
    var map = new HashMap<String, Object>();
    map.put("value", API);

    when(metadata.getAnnotationAttributes(Affinity.class.getName()))
      .thenReturn(map);
    when(context.getEnvironment()).thenReturn(environment);
    when(environment.acceptsProfiles(Profiles.of("api")))
      .thenReturn(true);

    var result = selector.matches(context, metadata);

    assertThat(result, is(true));
  }

  @Test
  void testNoneAffinity() {
    var map = new HashMap<String, Object>();
    map.put("value", NONE);

    when(metadata.getAnnotationAttributes(Affinity.class.getName()))
      .thenReturn(map);
    when(context.getEnvironment()).thenReturn(environment);
    when(environment.acceptsProfiles(Profiles.of("none"))).thenReturn(false);

    var result = selector.matches(context, metadata);

    assertThat(result, is(false));
  }
}
