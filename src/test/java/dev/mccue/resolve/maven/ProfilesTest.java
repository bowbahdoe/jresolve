package dev.mccue.resolve.maven;

import dev.mccue.resolve.*;
import dev.mccue.resolve.maven.MavenCoordinateId;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProfilesTest {
    @Test
    @Disabled("Profiles not yet supported")
    public void testJDKActivation() throws IOException {
        var dir = Files.createTempDirectory("temp");
        var result = new Resolve()
                .withCache(Cache.standard(dir))
                .addDependency(Dependency.mavenCentral("com.zaxxer:HikariCP:5.0.1"))
                .run();

        // Should get 2.0.0 for slf4j
        assertEquals(
                result.selectedDependencies()
                        .stream()
                        .map(DependencyId::new)
                        .collect(Collectors.toSet()),
                Set.of(
                        new DependencyId(
                                new Library("com.zaxxer", "HikariCP"),
                                new MavenCoordinateId(
                                        new Group(""),
                                        new Artifact(""),
                                        new Version("5.0.1"))
                        ),
                        new DependencyId(
                                new Library("org.slf4j", "slf4j-api"),
                                // 1.7.30 without considering the activation part
                                new MavenCoordinateId(
                                        new Group("org.slf4j"),
                                        new Artifact("slf4j-api"),
                                        new Version("2.0.0"))
                        )
                ));
    }
}
