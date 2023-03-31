package dev.mccue.resolve;

import dev.mccue.resolve.maven.MavenCoordinate;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static dev.mccue.resolve.maven.MavenRepository.MAVEN_CENTRAL;

public class CacheTest {
    @Test
    public void testMavenCache() {
        var coord = new MavenCoordinate(new Version("1.11.0"), MAVEN_CENTRAL);
        var cache = new StandardCache(Path.of("./libs"));

        var location = coord.getLibraryLocation(new Library("org.clojure", "clojure"), cache);
        System.out.println(location);
    }
}
