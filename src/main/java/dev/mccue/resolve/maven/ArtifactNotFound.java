package dev.mccue.resolve.maven;

import dev.mccue.resolve.Library;
import dev.mccue.resolve.Version;

final class ArtifactNotFound extends RuntimeException {
    final Library library;
    final Version version;

    ArtifactNotFound(Library library) {
        this.library = library;
        this.version = null;
    }

    ArtifactNotFound(Library library, Version version) {
        this.library = library;
        this.version = version;
    }

    ArtifactNotFound(Throwable throwable, Library library, Version version) {
        super(throwable);
        this.library = library;
        this.version = version;
    }

    @Override
    public String getMessage() {
        return "LibraryNotFound[library=" + library + ", version=" + version + "]";
    }
}
