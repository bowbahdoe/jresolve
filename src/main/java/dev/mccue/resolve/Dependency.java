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

    @Deprecated(forRemoval = true)
    public static Dependency maven(String coordinate, MavenRepository repository) {
        return maven(coordinate, List.of(repository));
    }

    @Deprecated(forRemoval = true)
    public static Dependency maven(String coordinateString, List<MavenRepository> repositories) {
        var parts = coordinateString.split(":");
        if (parts.length != 3 || parts[0].isBlank() || parts[1].isBlank() || parts[2].isBlank()) {
            throw new IllegalArgumentException(coordinateString + " does not fit the group:artifact:version format");
        }

        return new Dependency(
                new Library(parts[0], parts[1]),
                new MavenCoordinate(
                        new Group(parts[0]),
                        new Artifact(parts[1]),
                        new Version(parts[2]),
                        repositories
                )
        );
    }


    public static Dependency maven(
            Group group,
            Artifact artifact,
            Version version,
            List<MavenRepository> repositories
    ) {
        return new Dependency(
                new Library(group, artifact),
                new MavenCoordinate(
                        group,
                        artifact,
                        version,
                        repositories
                )
        );
    }

    public static Dependency maven(
            Group group,
            Artifact artifact,
            Version version,
            MavenRepository repository
    ) {
        return maven(group, artifact, version, List.of(repository));
    }

    public static Dependency maven(
            String group,
            String artifact,
            String version,
            List<MavenRepository> repositories
    ) {
        return maven(
                new Group(group),
                new Artifact(artifact),
                new Version(version),
                repositories
        );
    }

    public static Dependency maven(
            String group,
            String artifact,
            String version,
            MavenRepository repository
    ) {
        return maven(group, artifact, version, List.of(repository));
    }

    @Deprecated(forRemoval = true)
    public static Dependency mavenCentral(String coordinate) {
        return maven(coordinate, MavenRepository.central());
    }


    public static Dependency mavenCentral(Group group, Artifact artifact, Version version) {
        return maven(
                group,
                artifact,
                version,
                List.of(MavenRepository.central())
        );
    }

    public static Dependency mavenCentral(String group, String artifact, String version) {
        return maven(
                new Group(group),
                new Artifact(artifact),
                new Version(version),
                List.of(MavenRepository.central())
        );
    }

    public Dependency withExclusions(Exclusions exclusions) {
        return new Dependency(library, coordinate, exclusions);
    }
}
