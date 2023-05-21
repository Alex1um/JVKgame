package Game.Abilities;

import java.lang.annotation.*;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BasicAbilityMethod {
    String name();
}
