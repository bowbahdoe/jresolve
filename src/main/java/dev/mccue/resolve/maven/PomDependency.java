package dev.mccue.resolve.maven;

import dev.mccue.resolve.Library;
import dev.mccue.resolve.Variant;

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

    Library asLibraryOrThrow() {
        return new Library(
                this.groupId.orElseThrow(),
                this.artifactId().orElseThrow(),
                new Variant(this.classifier.orElse(Classifier.EMPTY).value())
        );
    }
}
