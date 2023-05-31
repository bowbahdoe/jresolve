package dev.mccue.resolve;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ResolveTest {
    record FakeManifest(
            @Override List<Dependency> dependencies
    ) implements Manifest {}

    record FakeCoordinateId(int version) implements CoordinateId {}

    record FakeCoordinate(String artifact, int version, Manifest manifest) implements Coordinate {
        @Override
        public VersionOrdering compareVersions(Coordinate coordinate) {
            return coordinate instanceof FakeCoordinate fake
                    ? VersionOrdering.fromInt(Integer.compare(this.version, fake.version))
                    : VersionOrdering.INCOMPARABLE;
        }

        @Override
        public CoordinateId id() {
            return new FakeCoordinateId(version);
        }

        @Override
        public Manifest getManifest(Cache cache) {
            return manifest;
        }

        @Override
        public Path getLibraryLocation(Cache cache) {
            return Path.of(".", artifact, Integer.toString(version));
        }
    }

    static Dependency fake(String artifact, int version, List<Dependency> manifest) {
        return new Dependency(new Library("ex", artifact), new FakeCoordinate(
                artifact,
                version,
                new FakeManifest(manifest)
        ));
    }

    static Dependency fake(String artifact, int version, List<Dependency> manifest, Exclusions exclusions) {
        return new Dependency(new Library("ex", artifact), new FakeCoordinate(artifact, version, new FakeManifest(manifest)), exclusions);
    }

    static Dependency fake(String artifact, int version) {
        return fake(artifact, version, List.of());
    }

    static Library fakeLib(String artifact) {
        return new Library("ex", artifact);
    }

    @Test
    public void testPickTop() {
        var resolution = new Resolve()
                .addDependency(
                        fake("A", 1, List.of(
                                fake("B", 2),
                                fake("C", 2)
                        ))
                )
                .addDependency(
                        fake("B", 1)
                )

                .run();

        assertEquals(
                resolution.versionMap()
                        .selectedVersion(fakeLib("B")),
                Optional.of(new FakeCoordinateId(1))
        );
    }

    @Test
    public void testExclusions() {
        var dep = fake(
                "A", 1, List.of(
                   fake("B", 1, List.of(
                           fake("C", 1, List.of(
                                   fake("X", 1, List.of()),
                                   fake( "Y", 1, List.of()),
                                   fake( "Z", 1, List.of())
                           ), Exclusions.of(
                                   new Exclusion(Group.ALL, new Artifact("X")),
                                   new Exclusion(Group.ALL, new Artifact("Y"))
                           ))
                   )),
                   fake( "D", 1, List.of(
                           fake("C", 1, List.of(
                                           fake( "X", 1, List.of()),
                                           fake( "Y", 1, List.of()),
                                           fake( "Z", 1, List.of())
                           ), Exclusions.of(
                                   new Exclusion(Group.ALL, new Artifact("X"))
                           )
                   ))
                )));

        var resolution = new Resolve().addDependency(dep).run();

        assertEquals(
                resolution.versionMap()
                        .selectedCoordinateIds(),
                Map.of(
                        fakeLib("A"), new FakeCoordinateId(1),
                        fakeLib("B"), new FakeCoordinateId(1),
                        fakeLib("C"), new FakeCoordinateId(1),
                        fakeLib("D"), new FakeCoordinateId(1),
                        fakeLib("Y"), new FakeCoordinateId(1),
                        fakeLib("Z"), new FakeCoordinateId(1)
                )
        );
    }

    /*
    ;; +a1 -> +b1 -> -c1
    ;;     -> +c2
     */
    @Test
    public void testDepChoice() {
        var dep = fake("A", 1, List.of(
                fake("B", 1, List.of(
                        fake("C", 1)
                )),
                fake("C", 2)
        ));

        var vmap = new Resolve().addDependency(dep)
                .run()
                .versionMap();

        assertEquals(
                Map.of(
                        fakeLib("A"), new FakeCoordinateId(1),
                        fakeLib("B"), new FakeCoordinateId(1),
                        fakeLib("C"), new FakeCoordinateId(2)
                ),
                vmap.selectedCoordinateIds()
        );
        vmap.printPrettyString();
    }

    /*
    ;; -> +a1 -> +d1
    ;; -> +b1 -> -e1 -> -d2
    ;; -> +c1 -> +e2
     */
    @Test
    public void testDepParentMissing() {
        var vmap = new Resolve()
                .addDependency(fake("A", 1, List.of(fake("D", 1))))
                .addDependency(fake("B", 1, List.of(fake("E", 1, List.of(fake("D", 2))))))
                .addDependency(fake("C", 1, List.of(fake("E", 2))))
                .run()
                .versionMap();

        assertEquals(
                Map.of(
                        fakeLib("A"), new FakeCoordinateId(1),
                        fakeLib("B"), new FakeCoordinateId(1),
                        fakeLib("C"), new FakeCoordinateId(1),
                        fakeLib("D"), new FakeCoordinateId(1),
                        fakeLib("E"), new FakeCoordinateId(2)
                ),
                vmap.selectedCoordinateIds()
        );
    }

    /*
    ;; +a1 -> +b1 -> +x2 -> +y1
    ;; +c1 -> -x1 -> -z1
     */
    @Test
    public void testDepChoice2() {
        var vmap = new Resolve()
                .addDependency(fake("A", 1, List.of(
                        fake("B", 1, List.of(
                                fake("X", 2, List.of(
                                        fake("Y", 1)
                                ))
                        )))))
                .addDependency(fake("C", 1, List.of(
                        fake("X", 1, List.of(
                                fake("Z", 1)
                        ))
                )))
                .run()
                .versionMap();

        assertEquals(
                Map.of(
                        fakeLib("A"), new FakeCoordinateId(1),
                        fakeLib("B"), new FakeCoordinateId(1),
                        fakeLib("C"), new FakeCoordinateId(1),
                        fakeLib("X"), new FakeCoordinateId(2),
                        fakeLib("Y"), new FakeCoordinateId(1)
                ),
                vmap.selectedCoordinateIds()
        );
    }

    /*
    ;; c1 included via both a and b, with exclusions in one branch and without in the other
    ;; should always include d1
    ;; +a1 -> +c1 (excl d) -> d1
    ;; +b1 -> +c1 -> +d1
     */
    @Test
    public void testSameVersionDifferentExclusions() {
        var r1 = new Resolve()
                .addDependency(fake("A", 1, List.of(
                        fake("C", 1, List.of(
                                fake("D", 1)
                        ), Exclusions.of(
                                new Exclusion(Group.ALL, new Artifact("D"))
                        )))))
                .addDependency(fake("B", 1, List.of(
                        fake("C", 1, List.of(
                                fake("D", 1)
                        ))
                )))
                .run();
        var vmap1 = r1.versionMap();
        var vmap2 = new Resolve()
                .addDependency(fake("B", 1, List.of(
                        fake("C", 1, List.of(
                                fake("D", 1)
                        ))
                )))
                .addDependency(fake("A", 1, List.of(
                        fake("C", 1, List.of(
                                fake("D", 1)
                        ), Exclusions.of(
                                new Exclusion(Group.ALL, new Artifact("D"))
                        )))))
                .run()
                .versionMap();

        var expected = Map.of(
                fakeLib("A"), new FakeCoordinateId(1),
                fakeLib("B"), new FakeCoordinateId(1),
                fakeLib("C"), new FakeCoordinateId(1),
                fakeLib("D"), new FakeCoordinateId(1)
        );

        assertEquals(
                expected,
                vmap1.selectedCoordinateIds()
        );

        assertEquals(
                expected,
                vmap2.selectedCoordinateIds()
        );
    }

    /*
    ;; +a1 -> +b1 -> -c1 -> a1
    ;;     -> +c2 -> a1
     */
    @Test
    public void testCircularDeps() {
        var aDeps = new ArrayList<Dependency>();
        aDeps.add(fake("B", 1, List.of(fake("C", 1, aDeps))));
        aDeps.add(fake("C", 2, aDeps));
        var vmap = new Resolve()
                .addDependency(fake("A", 1, aDeps))
                .run()
                .versionMap();

        assertEquals(
                Map.of(
                        fakeLib("A"), new FakeCoordinateId(1),
                        fakeLib("B"), new FakeCoordinateId(1),
                        fakeLib("C"), new FakeCoordinateId(2)
                ),
                vmap.selectedCoordinateIds()
        );
    }

    /*
    ;; +a1 -> -d1 -> -e1
    ;; +b1 -> +c1 -> +d2
    ;; e1 is found and selected due to d1, then cut when d2 is found
     */
    @Test
    public void testCutPreviouslySelectedChild() {
        var vmap = new Resolve()
                .addDependency(fake("A", 1, List.of(
                        fake("D", 1, List.of(
                                fake("E", 1)
                        ))
                )))
                .addDependency(fake("B", 1, List.of(
                        fake("C", 1, List.of(
                                fake("D", 2)
                        ))
                )))
                .run()
                .versionMap();
        assertEquals(
                Map.of(
                        fakeLib("A"), new FakeCoordinateId(1),
                        fakeLib("B"), new FakeCoordinateId(1),
                        fakeLib("C"), new FakeCoordinateId(1),
                        fakeLib("D"), new FakeCoordinateId(2)
                ),
                vmap.selectedCoordinateIds()
        );
    }

    /*
    ;; +a1 -> -d1 -> -e1 -> -f1
    ;; +b1 -> +c1 -> +g1 -> +d2 -> +e2
    ;; e1 is found and selected due to d1, then cut when d2 is found
     */
    @Test
    public void cutPreviouslySelectedChild2() {
        var vmap = new Resolve()
                .addDependency(fake("A", 1, List.of(
                        fake("D", 1, List.of(
                                fake("E", 1, List.of(
                                        fake("F", 1)
                                ))
                        ))
                )))
                .addDependency(fake("B", 1, List.of(
                        fake("C", 1, List.of(
                                fake("G", 1, List.of(
                                        fake("D", 2, List.of(
                                                fake("E", 2)
                                        ))
                                ))
                        ))
                )))
                .run()
                .versionMap();
        assertEquals(
                Map.of(
                        fakeLib("A"), new FakeCoordinateId(1),
                        fakeLib("B"), new FakeCoordinateId(1),
                        fakeLib("C"), new FakeCoordinateId(1),
                        fakeLib("D"), new FakeCoordinateId(2),
                        fakeLib("E"), new FakeCoordinateId(2),
                        fakeLib("G"), new FakeCoordinateId(1)
                ),
                vmap.selectedCoordinateIds()
        );
    }

    /*
    ;; +a -> +b -> -x2 -> -y2 -> -z2
    ;;    -> +c -> +d -> +x3 -> +y2 -> +z2
    ;;    -> -x1 -> -y1 -> -z1
    ;; include all of x3/y3/z3
     */
    @Test
    public void testMultiVersionDiscovery() {
        var vmap = new Resolve()
                .addDependency(fake(
                        "A", 1, List.of(
                                fake("B", 1, List.of(
                                        fake("X", 2, List.of(
                                                fake("Y", 2, List.of(
                                                        fake("Z", 2)
                                                ))
                                        ))
                                )),
                                fake("C", 1, List.of(
                                        fake("D", 1, List.of(
                                                fake("X", 3, List.of(
                                                        fake("Y", 2, List.of(
                                                                fake("Z", 2)
                                                        ))
                                                ))
                                        ))
                                )),
                                fake("X", 1, List.of(
                                        fake("Y", 1, List.of(
                                                fake("Z", 1)
                                        ))
                                ))
                        )
                ))
                .run()
                .versionMap();

        vmap.printPrettyString();
        assertEquals(
                Map.of(
                        fakeLib("A"), new FakeCoordinateId(1),
                        fakeLib("B"), new FakeCoordinateId(1),
                        fakeLib("C"), new FakeCoordinateId(1),
                        fakeLib("D"), new FakeCoordinateId(1),
                        fakeLib("X"), new FakeCoordinateId(3),
                        fakeLib("Y"), new FakeCoordinateId(2),
                        fakeLib("Z"), new FakeCoordinateId(2)
                ),
                vmap.selectedCoordinateIds()
        );
    }

    /*
    ;; +x1 -> -a1 -> +b2
    ;; +z1 -> +y1 -> +a2 -> -b1 (or +b1, but at least a consistent result)
    ;; TDEPS-58
     */
    @Test
    public void testDepOrdering() {
        var x = fake("X", 1, List.of(
                fake("A", 1, List.of(
                        fake("B", 2)
                ))
        ));
        var z = fake("Z", 1, List.of(
                fake("Y", 1, List.of(
                        fake("A", 2, List.of(
                                fake("B", 1)
                        ))
                ))
        ));

        assertEquals(
                new Resolve()
                        .addDependency(x)
                        .addDependency(z)
                        .run()
                        .versionMap()
                        .selectedCoordinateIds(),
                new Resolve()
                        .addDependency(z)
                        .addDependency(x)
                        .run()
                        .versionMap()
                        .selectedCoordinateIds()
        );
    }

    @Test
    public void testExclusionsGeolykt() {
        // Exclusions example problem provided by geolkyt on the rife2 discord
        var a =  fake("A", 1, List.of(
                fake("B", 1, List.of(
                        fake("D", 1, List.of(
                                fake("G", 1),
                                fake("F", 1)
                        ))
                )),
                fake("C", 1, List.of(
                        fake("E", 1, List.of(
                                fake("G", 1)
                        ))
                ), Exclusions.of(new Exclusion(Group.ALL, new Artifact("G"))))
        ), Exclusions.of(new Exclusion(Group.ALL, new Artifact("F"))));

        assertEquals(
                Map.of(
                        fakeLib("A"), new FakeCoordinateId(1),
                        fakeLib("B"), new FakeCoordinateId(1),
                        fakeLib("C"), new FakeCoordinateId(1),
                        fakeLib("D"), new FakeCoordinateId(1),
                        fakeLib("E"), new FakeCoordinateId(1),
                        fakeLib("G"), new FakeCoordinateId(1)
                ),
                new Resolve()
                        .addDependency(a)
                        .run()
                        .versionMap()
                        .selectedCoordinateIds()
        );
    }
}
