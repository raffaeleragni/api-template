package app.affinity;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.core.env.Profiles;
import org.springframework.core.type.AnnotatedTypeMetadata;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Conditional(AffinitySelector.class)
public @interface Affinity {
  enum Values {NONE, API, QUEUE}
  Values value();
}

class AffinitySelector implements Condition {

  @Override
  public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
    var profiles = extractProfiles(metadata);
    return profiles
      .map(p -> context.getEnvironment().acceptsProfiles(p))
      .orElse(true);
  }

  private static Optional<Profiles> extractProfiles(AnnotatedTypeMetadata metadata) {
    return Optional.ofNullable(metadata.getAnnotationAttributes(Affinity.class.getName()))
      .flatMap(a -> Optional.ofNullable(a.get("value")))
      .map(Object::toString)
      .map(String::toLowerCase)
      .flatMap(s -> Optional.ofNullable(Profiles.of(s)));
  }
}
