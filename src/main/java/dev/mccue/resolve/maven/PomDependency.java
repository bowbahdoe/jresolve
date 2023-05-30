package dev.mccue.resolve.maven;

import dev.mccue.resolve.Library;
import dev.mccue.resolve.Variant;

import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    PomDependency(
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

    PomDependency map(Function<String, String> resolve) {
        return new PomDependency(
                this.groupId().map(resolve),
                this.artifactId().map(resolve),
                this.version().map(resolve),
                this.exclusions().stream()
                        .map(exclusion -> new PomExclusion(
                                exclusion.groupId().map(resolve),
                                exclusion.artifactId().map(resolve)
                        ))
                        .collect(Collectors.toUnmodifiableSet()),
                this.type().map(resolve),
                this.classifier().map(resolve),
                this.optional().map(resolve),
                this.scope().map(resolve)
        );
    }
}
