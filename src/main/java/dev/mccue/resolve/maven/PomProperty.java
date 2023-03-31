package dev.mccue.resolve.maven;

import java.util.Objects;

record PomProperty(String key, String value) {
    public PomProperty {
        Objects.requireNonNull(key);
        Objects.requireNonNull(value);
    }
}
