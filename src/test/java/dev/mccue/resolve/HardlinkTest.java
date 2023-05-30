package dev.mccue.resolve;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

public class HardlinkTest {
    @Test
    public void createHardlinks() throws IOException {
        var resolved = new Resolve()
                .addDependencies(List.of(
                        Dependency.mavenCentral("dev.mccue:json:0.2.3")
                ))
                .fetch()
                .run();

        var libs = Path.of("libs");
        try {
            Files.walkFileTree(libs, new SimpleFileVisitor<>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    Files.delete(dir);
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (NoSuchFileException e) {}


        Files.createDirectories(libs);
        for (var path : resolved.libraries().values()) {
            Files.createLink(Path.of(libs.toString(), path.getFileName().toString()), path);
        }

    }
}
