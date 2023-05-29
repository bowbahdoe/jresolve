package dev.mccue.resolve.maven;

import dev.mccue.resolve.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MavenResolveTest {
    @Test
    public void testGetCompileTransitiveDependenciesJetty() throws IOException {
        var jetty = Dependency.mavenCentral("org.eclipse.jetty:jetty-server:11.0.14");

        var dependencies = new Resolve()
                .addDependency(jetty)
                .withCache(Cache.standard(Files.createTempDirectory("junit")))
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
    }

    @Test
    public void testDivergentRepos() throws IOException {
        /*
        clj -Sdeps '{:deps {org.clojure/clojure {:mvn/version "1.11.0"} ring/ring {:mvn/version "1.9.2"}}}}' -Stree

        org.clojure/clojure 1.11.0
          . org.clojure/spec.alpha 0.3.218
          . org.clojure/core.specs.alpha 0.2.62
        ring/ring 1.9.2
          . ring/ring-core 1.9.2
            . ring/ring-codec 1.1.3
              . commons-codec/commons-codec 1.15
            . commons-io/commons-io 2.6
            . commons-fileupload/commons-fileupload 1.4
              X commons-io/commons-io 2.2 :older-version
            . crypto-random/crypto-random 1.2.0
              X commons-codec/commons-codec 1.6 :older-version
            . crypto-equality/crypto-equality 1.0.0
          . ring/ring-devel 1.9.2
            . ring/ring-core 1.9.2
            . hiccup/hiccup 1.0.5
            . clj-stacktrace/clj-stacktrace 0.2.8
            . ns-tracker/ns-tracker 0.4.0
              . org.clojure/tools.namespace 0.2.11
              . org.clojure/java.classpath 0.3.0
          . ring/ring-jetty-adapter 1.9.2
            . ring/ring-core 1.9.2
            . ring/ring-servlet 1.9.2
            . org.eclipse.jetty/jetty-server 9.4.38.v20210224
              . javax.servlet/javax.servlet-api 3.1.0
              . org.eclipse.jetty/jetty-http 9.4.38.v20210224
                . org.eclipse.jetty/jetty-util 9.4.38.v20210224
                . org.eclipse.jetty/jetty-io 9.4.38.v20210224
              . org.eclipse.jetty/jetty-io 9.4.38.v20210224
                . org.eclipse.jetty/jetty-util 9.4.38.v20210224
          . ring/ring-servlet 1.9.2
            . ring/ring-core 1.9.2
         */
        var tempDir = Files.createTempDirectory("temp");
        var clojars = MavenRepository.remote("https://repo.clojars.org");

        var resolution = new Resolve()
                .addDependency(Dependency.mavenCentral("org.clojure:clojure:1.11.0"))
                .addDependency(Dependency.maven("ring:ring:1.9.2", List.of(clojars, MavenRepository.central())))
                .withCache(Cache.standard(tempDir))
                .run();


        assertEquals(
                Map.ofEntries(
                        Map.entry(
                                new Library("org.clojure", "clojure"),
                                new MavenCoordinateId(new Version("1.11.0"))
                        ),

                        Map.entry(
                                new Library("org.clojure", "spec.alpha"),
                                new MavenCoordinateId(new Version("0.3.218"))
                        ),

                        Map.entry(
                                new Library("org.clojure", "core.specs.alpha"),
                                new MavenCoordinateId(new Version("0.2.62"))
                        ),

                        Map.entry(
                                new Library("ring", "ring"),
                                new MavenCoordinateId(new Version("1.9.2"))
                        ),

                        Map.entry(
                                new Library("ring", "ring-core"),
                                new MavenCoordinateId(new Version("1.9.2"))
                        ),

                        Map.entry(
                                new Library("ring", "ring-codec"),
                                new MavenCoordinateId(new Version("1.1.3"))
                        ),

                        Map.entry(
                                new Library("commons-codec", "commons-codec"),
                                new MavenCoordinateId(new Version("1.15"))
                        ),
                        Map.entry(
                                new Library("commons-io", "commons-io"),
                                new MavenCoordinateId(new Version("2.6"))
                        ),
                        Map.entry(
                                new Library("commons-fileupload", "commons-fileupload"),
                                new MavenCoordinateId(new Version("1.4"))
                        ),
                        Map.entry(
                                new Library("crypto-random", "crypto-random"),
                                new MavenCoordinateId(new Version("1.2.0"))
                        ),
                        Map.entry(
                                new Library("crypto-equality", "crypto-equality"),
                                new MavenCoordinateId(new Version("1.0.0"))
                        ),
                        Map.entry(
                                new Library("ring", "ring-devel"),
                                new MavenCoordinateId(new Version("1.9.2"))
                        ),
                        Map.entry(
                                new Library("hiccup", "hiccup"),
                                new MavenCoordinateId(new Version("1.0.5"))
                        ),
                        Map.entry(
                                new Library("clj-stacktrace", "clj-stacktrace"),
                                new MavenCoordinateId(new Version("0.2.8"))
                        ),
                        Map.entry(
                                new Library("ns-tracker", "ns-tracker"),
                                new MavenCoordinateId(new Version("0.4.0"))
                        ),
                        Map.entry(
                                new Library("org.clojure", "tools.namespace"),
                                new MavenCoordinateId(new Version("0.2.11"))
                        ),

                        Map.entry(
                                new Library("org.clojure", "java.classpath"),
                                new MavenCoordinateId(new Version("0.3.0"))
                        ),
                        Map.entry(
                                new Library("ring", "ring-jetty-adapter"),
                                new MavenCoordinateId(new Version("1.9.2"))
                        ),
                        Map.entry(
                                new Library("ring", "ring-servlet"),
                                new MavenCoordinateId(new Version("1.9.2"))
                        ),

                        Map.entry(
                                new Library("org.eclipse.jetty", "jetty-server"),
                                new MavenCoordinateId(new Version("9.4.38.v20210224"))
                        ),
                        Map.entry(
                                new Library("javax.servlet", "javax.servlet-api"),
                                new MavenCoordinateId(new Version("3.1.0"))
                        ),
                        Map.entry(
                                new Library("org.eclipse.jetty", "jetty-http"),
                                new MavenCoordinateId(new Version("9.4.38.v20210224"))
                        ),

                        Map.entry(
                                new Library("org.eclipse.jetty", "jetty-util"),
                                new MavenCoordinateId(new Version("9.4.38.v20210224"))
                        ),
                        Map.entry(
                                new Library("org.eclipse.jetty", "jetty-io"),
                                new MavenCoordinateId(new Version("9.4.38.v20210224"))
                        )
                ),
                resolution
                        .selectedDependencies()
                        .stream()
                        .collect(Collectors.toMap(
                                Dependency::library,
                                dependency -> dependency.coordinate().id()
                        ))
        );
    }
}
