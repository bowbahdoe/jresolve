package dev.mccue.resolve.maven;

import org.jspecify.annotations.NullMarked;

import java.util.Objects;

@NullMarked
record PomProperty(String key, String value) {
    public PomProperty {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
    }
}
