package dev.mccue.resolve.core;

import dev.mccue.resolve.doc.Coursier;
import dev.mccue.resolve.doc.MavenSpecific;

import java.util.Objects;

@Coursier("https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/core/Definitions.scala#L358-L364")
@MavenSpecific
public record SnapshotVersion(
        Classifier classifier,
        Extension extension,
        String value,
        Versions.LastUpdated updated
) {
    public SnapshotVersion {
        Objects.requireNonNull(classifier, "classifier must not be null");
        Objects.requireNonNull(extension, "extension must not be null");
        Objects.requireNonNull(value, "value must not be null");
        Objects.requireNonNull(updated, "updated must not be null");
    }
}
