package studio.sniffa.common.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** The annotated {@code String}'s length, or {@code Collection}'s size, must be within [min, max]. */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.RECORD_COMPONENT, ElementType.FIELD, ElementType.PARAMETER})
public @interface Size {
    int min() default 0;

    int max() default Integer.MAX_VALUE;
}
