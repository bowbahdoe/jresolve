package dev.mccue.resolve.maven;

import dev.mccue.resolve.*;
import org.junit.jupiter.api.Test;

import java.util.List;

public class HelidonTest {
    @Test
    public void testResolveHelidon() {
        // pkg:maven/io.helidon.http.media/helidon-http-media-jsonp@4.0.8
        var resolve = new Resolve()
                .addDependency(Dependency.maven(
                        new Group("io.helidon.http.media"),
                        new Artifact("helidon-http-media-jsonp"),
                        new Version("4.0.8"),
                        List.of(MavenRepository.MAVEN_CENTRAL)
                ));

        // Shouldn't crash!
        resolve.run();
    }
}
