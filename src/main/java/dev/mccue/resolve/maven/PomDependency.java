package dev.mccue.resolve.maven;

import java.util.Set;

record PomDependency(
        PomGroupId groupId,
        PomArtifactId artifactId,
        PomVersion version,
        Set<PomExclusion> exclusions,
        PomType type,
        PomClassifier classifier,
        PomOptionality optional,
        PomScope scope
) {

    public PomDependency(
            PomGroupId groupId,
            PomArtifactId artifactId,
            PomVersion version
    ) {
        this(
                groupId,
                artifactId,
                version,
                Set.of(),
                PomType.Undeclared.INSTANCE,
                PomClassifier.Undeclared.INSTANCE,
                PomOptionality.Undeclared.INSTANCE,
                PomScope.Undeclared.INSTANCE
        );
    }
}
