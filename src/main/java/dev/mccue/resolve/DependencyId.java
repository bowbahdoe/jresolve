package dev.mccue.resolve;


import java.util.Objects;

public record DependencyId(
        Library library,
        CoordinateId coordinateId
) {
    public DependencyId {
        Objects.requireNonNull(library, "library must not be null");
        Objects.requireNonNull(coordinateId, "coordinateId must not be null");
    }

    public DependencyId(Dependency dependency) {
        this(dependency.library(), dependency.coordinate().id());
    }
}
