package dev.mccue.resolve;


import java.nio.file.Path;
import java.util.Objects;

public record FetchedLibrary(
        Path path,
        Usages usages
) {
    public FetchedLibrary {
        Objects.requireNonNull(path);
        Objects.requireNonNull(usages);
    }
}
