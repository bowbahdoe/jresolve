package dev.mccue.resolve;

import dev.mccue.resolve.maven.MavenCoordinate;
import dev.mccue.resolve.maven.MavenRepository;

import java.util.List;
import java.util.Objects;

public record Dependency(
        Library library,
        Coordinate coordinate,
        Exclusions exclusions
) {
    public Dependency {
        Objects.requireNonNull(library, "library must not be null.");
        Objects.requireNonNull(coordinate, "coordinate must not be null.");
        Objects.requireNonNull(exclusions, "exclusions must not be null.");
    }

    public Dependency(Library library, Coordinate coordinate) {
        this(library, coordinate, Exclusions.NONE);
    }

    public static Dependency maven(String coordinate, MavenRepository repository) {
        return maven(coordinate, List.of(repository));
    }

    public static Dependency maven(String coordinate, List<MavenRepository> repositories) {
        var parts = coordinate.split(":");
        if (parts.length != 3 || parts[0].isBlank() || parts[1].isBlank() || parts[2].isBlank()) {
            throw new IllegalArgumentException(coordinate + " does not fit the group:artifact:version format");
        }

        return new Dependency(new Library(parts[0], parts[1]), new MavenCoordinate(parts[2], repositories));
    }

    public static Dependency mavenCentral(String coordinate) {
        return maven(coordinate, MavenRepository.central());
    }

    Dependency withExclusions(Exclusions exclusions) {
        return new Dependency(library, coordinate, exclusions);
    }
}
