package dev.mccue.resolve.maven;

import dev.mccue.resolve.Cache;
import dev.mccue.resolve.Dependency;
import dev.mccue.resolve.Resolve;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

public class NoCacheTest {
    @Test
    public void testNoCache() throws IOException {
        var tempDir = Files.createTempDirectory("temp");

        var resolution1 = new Resolve()
                .addDependency(Dependency.mavenCentral("org.clojure:clojure:1.11.0"))
                .withCache(Cache.standard(tempDir))
                .run();

        var resolution2 = new Resolve()
                .addDependency(Dependency.mavenCentral("org.clojure:clojure:1.11.0"))
                .withCache(null)
                .run();

        var baos1 = new ByteArrayOutputStream();
        var baos2 = new ByteArrayOutputStream();
        resolution1.printTree(new PrintStream(baos1), List.of());
        resolution2.printTree(new PrintStream(baos2), List.of());

        assertArrayEquals(baos1.toByteArray(), baos2.toByteArray());
    }
}
