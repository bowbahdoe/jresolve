package dev.mccue.resolve.maven;

import dev.mccue.resolve.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RemoteMavenRepositoryTest {
    @Test
    public void getBasicMetadata() throws IOException {
        var metadata = MavenRepository.central()
                .getMavenMetadata(new Group("dev.mccue"), new Artifact("async"));
        assertEquals(new MavenMetadata(
                new Group("dev.mccue"),
                new Artifact("async"),
                new Version("0.1.0"),
                new Version("0.1.0"),
                List.of(
                        new Version("0.0.1653933964"),
                        new Version("0.0.1653934603"),
                        new Version("0.0.1653940357"),
                        new Version("0.0.1653940651"),
                        new Version("0.0.1653946197"),
                        new Version("0.0.1653946560"),
                        new Version("0.0.1653946767"),
                        new Version("0.0.1653946908"),
                        new Version("0.1.0")
                ),
                LocalDateTime.of(2022, 5, 30, 21, 51, 29)
        ), metadata);
    }
}
