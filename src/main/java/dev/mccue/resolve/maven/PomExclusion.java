package dev.mccue.resolve.maven;

import java.util.function.Function;

record PomExclusion(
        PomGroupId groupId,
        PomArtifactId artifactId
) {

}