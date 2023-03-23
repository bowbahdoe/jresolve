package dev.mccue.resolve.maven;

import dev.mccue.resolve.CoordinateId;
import dev.mccue.resolve.VersionNumber;

public record MavenCoordinateId(VersionNumber version)
        implements CoordinateId {
}
