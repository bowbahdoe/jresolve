package dev.mccue.resolve.maven;

import dev.mccue.resolve.Artifact;
import dev.mccue.resolve.CoordinateId;
import dev.mccue.resolve.Group;
import dev.mccue.resolve.Version;

public record MavenCoordinateId(
        Group group,
        Artifact artifact,
        Version version
)
        implements CoordinateId {

    @Override
    public String toString() {
        return version.toString();
    }
}
