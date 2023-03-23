package dev.mccue.resolve;

import java.util.Objects;

public record Dependency(
        Library library,
        Coordinate coordinate
) {
    public Dependency {
        Objects.requireNonNull(library, "library must not be null.");
        Objects.requireNonNull(coordinate, "coordinate must not be null.");
    }
}
