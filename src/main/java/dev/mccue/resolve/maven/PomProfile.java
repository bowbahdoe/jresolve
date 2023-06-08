package dev.mccue.resolve.maven;

import java.util.List;
import java.util.Optional;

public record PomProfile(
        String id,
        Optional<Boolean> activeByDefault,
        PomActivation activation,
        List<PomDependency> dependencies,
        List<PomDependency> dependencyManagement,
        List<PomProperty> properties
) {
}
