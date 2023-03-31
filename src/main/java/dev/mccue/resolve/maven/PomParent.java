package dev.mccue.resolve.maven;

import java.util.Objects;
import java.util.function.Function;

sealed interface PomParent {
    enum Undeclared implements PomParent {
        INSTANCE;

        @Override
        public String toString() {
            return "Undeclared[]";
        }
    }

    record Declared(
            PomGroupId.Declared groupId,
            PomArtifactId.Declared artifactId,
            PomVersion.Declared version
    ) implements PomParent {
        public Declared {
            Objects.requireNonNull(groupId);
            Objects.requireNonNull(artifactId);
            Objects.requireNonNull(version);
        }
    }
}
