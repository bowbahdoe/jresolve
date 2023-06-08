package dev.mccue.resolve.maven;

import dev.mccue.resolve.Cache;
import dev.mccue.resolve.Dependency;
import dev.mccue.resolve.Library;
import dev.mccue.resolve.Resolve;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MavenFetchTest {
    @Test
    public void fetchIntoStandardCache() throws IOException {
        var temp = Files.createTempDirectory("temp");
        var clojure = Dependency.mavenCentral("org.clojure:clojure:1.11.0");

        var libraries = new Resolve()
                .addDependency(clojure)
                .withCache(Cache.standard(temp))
                .fetch()
                .run()
                .libraries();

        assertEquals(
                Map.of(
                        new Library("org.clojure", "clojure"),
                        Path.of(
                                temp.toString(),
                                "https",
                                "repo1.maven.org",
                                "maven2",
                                "org",
                                "clojure",
                                "clojure",
                                "1.11.0",
                                "clojure-1.11.0.jar"
                        ),
                        new Library("org.clojure", "spec.alpha"),
                        Path.of(
                                temp.toString(),
                                "https",
                                "repo1.maven.org",
                                "maven2",
                                "org",
                                "clojure",
                                "spec.alpha",
                                "0.3.218",
                                "spec.alpha-0.3.218.jar"
                        ),
                        new Library("org.clojure", "core.specs.alpha"),
                        Path.of(
                                temp.toString(),
                                "https",
                                "repo1.maven.org",
                                "maven2",
                                "org",
                                "clojure",
                                "core.specs.alpha",
                                "0.2.62",
                                "core.specs.alpha-0.2.62.jar"
                        )
                ),
                libraries
        );
    }

    @Test
    public void fetchSources() throws IOException {
        var temp = Files.createTempDirectory("temp");
        var json = Dependency.mavenCentral("dev.mccue:json:0.2.3");

        var result = new Resolve()
                .addDependency(json)
                .withCache(Cache.standard(temp))
                .fetch()
                .includeSources()
                .includeDocumentation()
                .run();

        var sources = result.sources();


        assertEquals(
                Map.of(
                        new Library("dev.mccue", "json"),
                        Path.of(
                                temp.toString(),
                                "https",
                                "repo1.maven.org",
                                "maven2",
                                "dev",
                                "mccue",
                                "json",
                                "0.2.3",
                                "json-0.2.3-sources.jar"
                        )
                ),
                sources
        );
    }
}
