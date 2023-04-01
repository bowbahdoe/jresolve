package dev.mccue.resolve;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

public class ResolveTest {
    @Test
    public void basicResolve() {
        var resolution = new Resolve()
                .addDependencies(List.of(
                        Dependency.mavenCentral("org.clojure:clojure:1.11.0")
                ))
                .withCache(new StandardCache(Path.of("./ex")))
                .run();

        resolution.versionMap().printPrettyString();
    }

    record FakeManifest(
            @Override List<Dependency> dependencies
    ) implements Manifest {}

    record FakeCoordinateId(int version) implements CoordinateId {}

    record FakeCoordinate(int version, Manifest manifest) implements Coordinate {
        @Override
        public VersionComparison compareVersions(Coordinate coordinate) {
            return coordinate instanceof FakeCoordinate fake
                    ? VersionComparison.fromInt(Integer.compare(this.version, fake.version))
                    : VersionComparison.INCOMPARABLE;
        }

        @Override
        public CoordinateId id() {
            return new FakeCoordinateId(version);
        }

        @Override
        public Manifest getManifest(Library library, Cache cache) {
            return manifest;
        }

        @Override
        public Path getLibraryLocation(Library library, Cache cache) {
            return Path.of(".", library.toString(), Integer.toString(version));
        }
    }

    static Dependency fake(String group, String artifact, int version, List<Dependency> manifest) {
        return new Dependency(new Library(group, artifact), new FakeCoordinate(version, new FakeManifest(manifest)));
    }

    static Dependency fake(String group, String artifact, int version, List<Dependency> manifest, Exclusions exclusions) {
        return new Dependency(new Library(group, artifact), new FakeCoordinate(version, new FakeManifest(manifest)), exclusions);
    }

    static Dependency fake(String group, String artifact, int version) {
        return fake(group, artifact, version, List.of());
    }

    @Test
    public void testPickTop() {
        var resolution = new Resolve()
                .addDependency(
                        fake("org", "a", 1, List.of(
                                fake("org", "b", 2),
                                fake("org", "c", 2)
                        ))
                )
                .addDependency(
                        fake("org", "b", 1)
                )
                .run();

        resolution.versionMap().printPrettyString();
    }

    @Test
    public void testExclusions() {
        var dep = fake(
                "ex", "A", 1, List.of(
                   fake("ex", "B", 1, List.of(
                           fake("ex", "C", 1, List.of(
                                   fake("ex", "X", 1, List.of()),
                                   fake("ex", "Y", 1, List.of()),
                                   fake("ex", "Z", 1, List.of())
                           ), Exclusions.of(List.of(
                                   new Exclusion(Group.ALL, new Artifact("X")),
                                   new Exclusion(Group.ALL, new Artifact("Y"))
                           )))
                   )),
                   fake("ex", "D", 1, List.of(
                           fake("ex", "C", 1, List.of(
                                           fake("ex", "X", 1, List.of()),
                                           fake("ex", "Y", 1, List.of()),
                                           fake("ex", "Z", 1, List.of())
                           ), Exclusions.of(List.of(
                                   new Exclusion(Group.ALL, new Artifact("X"))
                           ))
                   ))
                )));

        new Resolve().addDependency(dep).run().versionMap().printPrettyString();

        System.out.println(Exclusions.of(List.of(
                new Exclusion(Group.ALL, new Artifact("X")),
                new Exclusion(Group.ALL, new Artifact("Y"))
        )));
    }
}
