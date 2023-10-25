package dev.mccue.resolve.doc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marker annotation for code we translated more or less
 * faithfully from Coursier. Its purpose is just to serve
 * as documentation for us.
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Coursier {
    /**
     * @return A reference to the place in Coursier's design you
     * are drawing from. If it is a GitHub link, try to use a
     * link to a particular hash instead of 'master' or 'main'.
     */
    String value();
}
