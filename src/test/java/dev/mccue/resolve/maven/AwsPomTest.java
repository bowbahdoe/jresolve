package dev.mccue.resolve.maven;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AwsPomTest {
    @Test
    public void parseAwsPom() throws IOException {
        var result = PomParser.parse(new String(Objects.requireNonNull(
                AwsPomTest.class.getResourceAsStream("/awspom.xml")
                )
                .readAllBytes(), StandardCharsets.UTF_8));

        // Validation that a bug where the version property from a BOM overrode the declared version.
        assertEquals(result.version(), new PomVersion.Declared("2.21.22"));
    }
}
