package dev.mccue.resolve.maven;

import dev.mccue.resolve.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MavenResolveTest {
    @Test
    public void testGetCompileTransitiveDependenciesJetty() throws IOException {
        var jetty = Dependency.mavenCentral("org.eclipse.jetty:jetty-server:11.0.14");

        var dependencies = new Resolve()
                .addDependency(jetty)
                .withCache(new StandardCache(Files.createTempDirectory("junit")))
                .run()
                .selectedDependencies();

        assertEquals(
                Map.of(
                        new Library("org.eclipse.jetty", "jetty-server"),
                        new MavenCoordinateId(new Version("11.0.14")),

                        new Library("org.eclipse.jetty.toolchain", "jetty-jakarta-servlet-api"),
                        new MavenCoordinateId(new Version("5.0.2")),

                        new Library("org.eclipse.jetty", "jetty-http"),
                        new MavenCoordinateId(new Version("11.0.14")),

                        new Library("org.eclipse.jetty", "jetty-io"),
                        new MavenCoordinateId(new Version("11.0.14")),

                        new Library("org.slf4j", "slf4j-api"),
                        new MavenCoordinateId(new Version("2.0.5")),

                        new Library("org.eclipse.jetty", "jetty-util"),
                        new MavenCoordinateId(new Version("11.0.14"))
                ),
                dependencies
                        .stream()
                        .collect(Collectors.toMap(
                                Dependency::library,
                                dependency -> dependency.coordinate().id()
                        ))
        );

        var classpath = new Fetch()
                .addDependency(Dependency.mavenCentral("dev.mccue:json:0.2.3"))
                .run()
                .classpath();

        System.out.println(classpath);
    }

    @Test
    public void testImpliedRepos() {
        var clojars = MavenRepository.remote("https://repo.clojars.org");

        var fetchResult = new Fetch()
                .addDependency(Dependency.mavenCentral("org.clojure:clojure:1.11.0"))
                .addDependency(Dependency.maven("ring:ring:1.9.2", clojars))
                .run();

        System.out.println(fetchResult
                .libraries()
                .stream()
                .map(Path::toString)
                .collect(Collectors.joining("\n"))
        );
    }
}
