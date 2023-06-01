package dev.mccue.resolve.maven;

import java.util.List;

public record PomProfile(
        String id,
        PomActivation activation,
        List<PomProperty> properties,
        List<PomDependency> dependencies
) {
}
