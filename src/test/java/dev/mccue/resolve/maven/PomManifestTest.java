package dev.mccue.resolve.maven;

import dev.mccue.resolve.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static dev.mccue.resolve.maven.RemoteMavenRepository.MAVEN_CENTRAL;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class PomManifestTest {
    @Test
    public void testClojure() throws IOException {
        var tempDir = Files.createTempDirectory("resolve");
        var clojureManifest = MAVEN_CENTRAL.getManifest(
                new Library("org.clojure", "clojure"),
                new Version("1.11.1"),
                new StandardCache(Path.of("./libs")),
                List.of(Scope.COMPILE)
        );

        assertEquals(clojureManifest, new PomManifest(
                List.of(
                        new Dependency(
                            new Library("org.clojure", "spec.alpha"),
                            new MavenCoordinate("0.3.218", MAVEN_CENTRAL)
                        ),
                        new Dependency(
                                new Library("org.clojure", "core.specs.alpha"),
                                new MavenCoordinate("0.2.62", MAVEN_CENTRAL)
                        )
                )
        ));
    }

    @Test
    public void testVaadin() throws IOException {
        var tempDir = Files.createTempDirectory("resolve");
        var vaadinManifest = MAVEN_CENTRAL.getManifest(
                new Library("com.vaadin", "vaadin"),
                new Version("23.3.7"),
                new StandardCache(Path.of("./libs"))
        );


        assertEquals(vaadinManifest, new PomManifest(List.of()));

        var vaadinCompileManifest = MAVEN_CENTRAL.getManifest(
                new Library("com.vaadin", "vaadin"),
                new Version("23.3.7"),
                new StandardCache(tempDir),
                List.of(Scope.COMPILE)
        );

        assertEquals(vaadinCompileManifest, new PomManifest(List.of(
                new Dependency(
                        new Library("com.vaadin", "vaadin-core"),
                        new MavenCoordinate("23.3.7", MAVEN_CENTRAL)
                ),
                new Dependency(
                        new Library("com.vaadin", "vaadin-board-flow"),
                        new MavenCoordinate("23.3.7", MAVEN_CENTRAL)
                ),
                new Dependency(
                        new Library("com.vaadin", "vaadin-charts-flow"),
                        new MavenCoordinate("23.3.7", MAVEN_CENTRAL)
                ),
                new Dependency(
                        new Library("com.vaadin", "vaadin-cookie-consent-flow"),
                        new MavenCoordinate("23.3.7", MAVEN_CENTRAL)
                ),
                new Dependency(
                        new Library("com.vaadin", "vaadin-crud-flow"),
                        new MavenCoordinate("23.3.7", MAVEN_CENTRAL)
                ),
                new Dependency(
                        new Library("com.vaadin", "vaadin-grid-pro-flow"),
                        new MavenCoordinate("23.3.7", MAVEN_CENTRAL)
                ),
                new Dependency(
                        new Library("com.vaadin", "vaadin-map-flow"),
                        new MavenCoordinate("23.3.7", MAVEN_CENTRAL)
                ),
                new Dependency(
                        new Library("com.vaadin", "vaadin-rich-text-editor-flow"),
                        new MavenCoordinate("23.3.7", MAVEN_CENTRAL)
                ),
                new Dependency(
                        new Library("com.vaadin", "collaboration-engine"),
                        new MavenCoordinate("5.3.0", MAVEN_CENTRAL)
                )
        )));
    }
}
