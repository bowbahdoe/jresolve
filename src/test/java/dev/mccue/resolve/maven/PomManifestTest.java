package dev.mccue.resolve.maven;

import dev.mccue.resolve.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;

import static dev.mccue.resolve.maven.MavenRepository.MAVEN_CENTRAL;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PomManifestTest {
    @Test
    public void testClojure() throws IOException {
        var tempDir = Files.createTempDirectory("resolve");
        var clojureManifest = MAVEN_CENTRAL.getManifest(
                new Group("org.clojure"),
                new Artifact("clojure"),
                new Version("1.11.1"),
                Cache.standard(tempDir),
                List.of(Scope.COMPILE),
                List.of(MAVEN_CENTRAL),
                Runtime.version(),
                new Os()
        );

        assertEquals(clojureManifest, new PomManifest(
                List.of(
                        new Dependency(
                            new Library("org.clojure", "spec.alpha"),
                            new MavenCoordinate(
                                    new Group("org.clojure"),
                                    new Artifact("spec.alpha"),
                                    new Version("0.3.218"),
                                    Collections.singletonList(MAVEN_CENTRAL)
                            )
                        ),
                        new Dependency(
                                new Library("org.clojure", "core.specs.alpha"),
                                new MavenCoordinate(
                                        new Group("org.clojure"),
                                        new Artifact("core.specs.alpha"),
                                        new Version("0.2.62"),
                                        Collections.singletonList(MAVEN_CENTRAL)
                                )
                        )
                )
        ));
    }

    @Test
    public void testVaadin() throws IOException {
        var tempDir = Files.createTempDirectory("resolve");
        var vaadinManifest = MAVEN_CENTRAL.getManifest(
                new Group("com.vaadin"),
                new Artifact("vaadin"),
                new Version("23.3.7"),
                Cache.standard(tempDir),
                List.of(),
                List.of(MAVEN_CENTRAL),
                Runtime.version(),
                new Os()
        );


        assertEquals(vaadinManifest, new PomManifest(List.of()));

        var vaadinCompileManifest = MAVEN_CENTRAL.getManifest(
                new Group("com.vaadin"),
                new Artifact("vaadin"),
                new Version("23.3.7"),
                Cache.standard(tempDir),
                List.of(Scope.COMPILE),
                List.of(MAVEN_CENTRAL),
                Runtime.version(),
                new Os()
        );

        assertEquals(vaadinCompileManifest, new PomManifest(List.of(
                new Dependency(
                        new Library("com.vaadin", "vaadin-core"),
                        new MavenCoordinate(
                                new Group("com.vaadin"),
                                new Artifact("vaadin-core"),
                                new Version("23.3.7"),
                                List.of(MAVEN_CENTRAL)
                        )
                ),
                new Dependency(
                        new Library("com.vaadin", "vaadin-board-flow"),
                        new MavenCoordinate(
                                new Group("com.vaadin"),
                                new Artifact("vaadin-board-flow"),
                                new Version("23.3.7"),
                                List.of(MAVEN_CENTRAL)
                        )
                ),
                new Dependency(
                        new Library("com.vaadin", "vaadin-charts-flow"),
                        new MavenCoordinate(
                                new Group("com.vaadin"),
                                new Artifact("vaadin-charts-flow"),
                                new Version("23.3.7"),
                                List.of(MAVEN_CENTRAL)
                        )
                ),
                new Dependency(
                        new Library("com.vaadin", "vaadin-cookie-consent-flow"),
                        new MavenCoordinate(
                                new Group("com.vaadin"),
                                new Artifact("vaadin-cookie-consent-flow"),
                                new Version("23.3.7"),
                                List.of(MAVEN_CENTRAL)
                        )
                ),
                new Dependency(
                        new Library("com.vaadin", "vaadin-crud-flow"),
                        new MavenCoordinate(
                                new Group("com.vaadin"),
                                new Artifact("vaadin-crud-flow"),
                                new Version("23.3.7"),
                                List.of(MAVEN_CENTRAL)
                        )
                ),
                new Dependency(
                        new Library("com.vaadin", "vaadin-grid-pro-flow"),
                        new MavenCoordinate(
                                new Group("com.vaadin"),
                                new Artifact("vaadin-grid-pro-flow"),
                                new Version("23.3.7"),
                                List.of(MAVEN_CENTRAL)
                        )
                ),
                new Dependency(
                        new Library("com.vaadin", "vaadin-map-flow"),
                        new MavenCoordinate(
                                new Group("com.vaadin"),
                                new Artifact("vaadin-map-flow"),
                                new Version("23.3.7"),
                                List.of(MAVEN_CENTRAL)
                        )
                ),
                new Dependency(
                        new Library("com.vaadin", "vaadin-rich-text-editor-flow"),
                        new MavenCoordinate(
                                new Group("com.vaadin"),
                                new Artifact("vaadin-rich-text-editor-flow"),
                                new Version("23.3.7"),
                                List.of(MAVEN_CENTRAL)
                        )
                ),
                new Dependency(
                        new Library("com.vaadin", "collaboration-engine"),
                        new MavenCoordinate(
                                new Group("com.vaadin"),
                                new Artifact("collaboration-engine"),
                                new Version("5.3.0"),
                                List.of(MAVEN_CENTRAL)
                        )
                )
        )));
    }
}
