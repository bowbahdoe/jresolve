package dev.mccue.resolve;

import java.util.Objects;

/**
 * Some libraries will be published onto a repository like maven
 * with multiple "variants" under the same group and artifact.
 *
 * <p>
 *     These different variants will generally be put under their own classifiers,
 *     but a classifier is a maven-specific concept. So for the purposes of resolution
 *     we track the variant as part of the library. Libraries with multiple variants are treated
 *     as entirely distinct entities for during resolution.
 * </p>
 * @param value
 */
public record Variant(String value) {
    public static final Variant DEFAULT = new Variant("");

    public Variant {
        Objects.requireNonNull(value);
    }
}
