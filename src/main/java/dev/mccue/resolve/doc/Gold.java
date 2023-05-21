package dev.mccue.resolve.doc;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to mark a class as "gold". As in, done for the initial release/publishing.
 *
 * <p>
 *     This very quickly will lose meaning after I do publish a release, but hopefully
 *     will be helpful for me in the meantime.
 * </p>
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface Gold {
}
