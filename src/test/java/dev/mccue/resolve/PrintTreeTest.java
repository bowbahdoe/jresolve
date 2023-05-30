package dev.mccue.resolve;

import dev.mccue.resolve.maven.MavenRepository;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PrintTreeTest {
    @Test
    public void testClojureAndRing() throws IOException {
        var tempDir = Files.createTempDirectory("temp");

        var baos = new ByteArrayOutputStream();
        new Resolve()

                .addDependency(Dependency.mavenCentral(
                        "org.clojure:clojure:1.11.0"
                ))
                .addDependency(Dependency.maven(
                        "ring:ring:1.9.3",
                        List.of(
                                MavenRepository.remote("https://repo.clojars.org"),
                                MavenRepository.central()
                        )
                ))
                .withCache(Cache.standard(tempDir))
                .run()
                .printTree(new PrintStream(baos), List.of(new Library("org.clojure", "clojure")));

        assertEquals(
                """
                        org.clojure/clojure 1.11.0
                          . org.clojure/spec.alpha 0.3.218
                          . org.clojure/core.specs.alpha 0.2.62
                        ring/ring 1.9.3
                          . ring/ring-core 1.9.3
                            . ring/ring-codec 1.1.3
                              . commons-codec/commons-codec 1.15
                            . commons-io/commons-io 2.6
                            . commons-fileupload/commons-fileupload 1.4
                              X commons-io/commons-io 2.2 OLDER_VERSION
                            . crypto-random/crypto-random 1.2.0
                              X commons-codec/commons-codec 1.6 OLDER_VERSION
                            . crypto-equality/crypto-equality 1.0.0
                          . ring/ring-devel 1.9.3
                            . ring/ring-core 1.9.3
                            . hiccup/hiccup 1.0.5
                            . clj-stacktrace/clj-stacktrace 0.2.8
                            . ns-tracker/ns-tracker 0.4.0
                              . org.clojure/tools.namespace 0.2.11
                              . org.clojure/java.classpath 0.3.0
                          . ring/ring-jetty-adapter 1.9.3
                            . ring/ring-core 1.9.3
                            . ring/ring-servlet 1.9.3
                            . org.eclipse.jetty/jetty-server 9.4.40.v20210413
                              . javax.servlet/javax.servlet-api 3.1.0
                              . org.eclipse.jetty/jetty-http 9.4.40.v20210413
                                . org.eclipse.jetty/jetty-util 9.4.40.v20210413
                                . org.eclipse.jetty/jetty-io 9.4.40.v20210413
                              . org.eclipse.jetty/jetty-io 9.4.40.v20210413
                                . org.eclipse.jetty/jetty-util 9.4.40.v20210413
                          . ring/ring-servlet 1.9.3
                            . ring/ring-core 1.9.3
                        """,

                baos.toString(StandardCharsets.UTF_8));
    }

    @Test
    public void testComplexProject() throws IOException {
        var tempDir = Files.createTempDirectory("temp");

        var repos = List.of(
                MavenRepository.central(),
                MavenRepository.remote("https://repo.clojars.org")

        );

        var baos = new ByteArrayOutputStream();


        new Resolve()
                .addDependency(Dependency.maven("org.clojure:clojure:1.11.0", repos))
                .addDependency(Dependency.maven("info.sunng:ring-jetty9-adapter:0.18.3", repos))
                .addDependency(Dependency.maven("hiccup:hiccup:2.0.0-alpha2", repos))
                .addDependency(Dependency.maven("metosin:reitit:0.5.18", repos))
                .addDependency(Dependency.maven("ring:ring-anti-forgery:1.3.0", repos))
                .addDependency(Dependency.maven("cheshire:cheshire:5.11.0", repos))
                .addDependency(Dependency.maven("com.github.seancorfield:next.jdbc:1.3.847", repos))
                .addDependency(Dependency.maven("com.github.seancorfield:honeysql:2.4.980", repos))
                .addDependency(Dependency.maven("metosin:malli:0.10.1", repos))
                .addDependency(Dependency.maven("org.postgresql:postgresql:42.5.4", repos))
                .addDependency(Dependency.maven("org.togglz:togglz-core:3.3.3", repos))
                .addDependency(Dependency.maven("msolli:proletarian:1.0.68-alpha", repos))
                .addDependency(Dependency.maven("ring:ring-defaults:0.3.4", repos))
                .addDependency(Dependency.maven("metosin:muuntaja:0.6.8", repos))
                .addDependency(Dependency.maven("org.slf4j:slf4j-simple:2.0.7", repos))
                .addDependency(Dependency.maven("net.ttddyy:datasource-proxy:1.8", repos))
                .addDependency(Dependency.maven("com.widdindustries:cljc.java-time:0.1.21", repos))
                .addDependency(Dependency.maven("json-html:json-html:0.4.7", repos))
                .addDependency(Dependency.maven("com.github.steffan-westcott:clj-otel-api:0.1.5", repos))
                .addDependency(Dependency.maven("org.joda:joda-money:1.0.3", repos))
                .addDependency(Dependency.maven("buddy:buddy-auth:3.0.323", repos))
                .addDependency(Dependency.maven("buddy:buddy-hashers:1.8.158", repos))
                .addDependency(Dependency.maven("clj-http:clj-http:3.12.3", repos))
                .addDependency(Dependency.maven("com.zaxxer:HikariCP:5.0.1", repos))
                .addDependency(Dependency.maven("resilience4clj:resilience4clj-retry:0.1.1", repos))
                .addDependency(Dependency.maven("dev.weavejester:medley:1.7.0", repos))
                .addDependency(Dependency.maven("com.auth0:auth0:2.1.0", repos))
                .withCache(Cache.standard(tempDir))
                .run()
                .printTree(new PrintStream(baos), List.of(new Library("org.clojure", "clojure")));

        System.out.println(baos.toString(StandardCharsets.UTF_8));
        assertEquals(
                281,
                baos.toString(StandardCharsets.UTF_8).split("\n").length
        );
    }
}


