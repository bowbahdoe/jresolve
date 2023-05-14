package dev.mccue.resolve.doc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks that some code or pattern came from Clojure's tools.deps
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.RECORD_COMPONENT, ElementType.TYPE})
public @interface ToolsDeps {
    String value();

    String details() default "";
}
