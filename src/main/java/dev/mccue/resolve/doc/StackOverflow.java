package dev.mccue.resolve.doc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that some code is taken from or derived from advice on
 * StackOverflow.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface StackOverflow {
    /**
     * @return The url of the stack overflow post or a link
     * to the specific answer.
     */
    String value();

    /**
     * @return Relevant details about what was taken from the
     * answer.
     */
    String details() default "";
}
