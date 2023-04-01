package dev.mccue.resolve;

import dev.mccue.resolve.maven.MavenCoordinate;
import dev.mccue.resolve.maven.MavenRepository;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;


public class CacheTest {
    @Test
    public void testMavenCache() {
        var coord = new MavenCoordinate(new Version("1.11.0"), MavenRepository.central());
        var cache = new StandardCache(Path.of("./libs"));

        var location = coord.getLibraryLocation(new Library("org.clojure", "clojure"), cache);
        System.out.println(location);
        var location2 = coord.getLibrarySourcesLocation(new Library("org.clojure", "clojure"), cache);
        System.out.println(location2);

        var location3 = coord.getLibraryDocumentationLocation(new Library("org.clojure", "clojure"), cache);
        System.out.println(location3);
    }
}
