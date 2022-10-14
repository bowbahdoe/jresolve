package dev.mccue.resolve.core;

import dev.mccue.resolve.doc.Coursier;
import dev.mccue.resolve.doc.Incomplete;
import dev.mccue.resolve.doc.MavenSpecific;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

@Incomplete
@Coursier("https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/core/Definitions.scala#L366-L377")
@MavenSpecific
public record SnapshotVersioning(
        Module module,
        String version,
        String latest,
        String release,
        String timestamp,
        OptionalInt buildNumber,
        Optional<Boolean> localCopy,
        Versions.LastUpdated lastUpdated,
        List<SnapshotVersion> snapshotVersions
) {
}
