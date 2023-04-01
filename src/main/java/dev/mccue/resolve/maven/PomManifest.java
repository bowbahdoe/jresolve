package dev.mccue.resolve.maven;

import dev.mccue.resolve.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;

record PomManifest(
        @Override List<Dependency> dependencies
) implements Manifest {
    PomManifest normalize(Cache cache) {
        return new PomManifest(dependencies
                .stream()
                .map(dependency -> new Dependency(
                        dependency.library(),
                        dependency.coordinate().normalize(dependency.library(), cache),
                        dependency.exclusions()
                ))
                .toList());
    }
    public static PomManifest from(
            EffectivePomInfo effectivePomInfo,
            List<Scope> scopes,
            BiFunction<Version, Exclusions, MavenCoordinate> makeCoordinate
    ) {
        var dependencies = new ArrayList<Dependency>();

        var dependencyManagement = new HashMap<ManagedDependencyKey, PomDependency>();
        for (var managedDependency : effectivePomInfo.dependencyManagement()) {
            dependencyManagement.put(ManagedDependencyKey.from(managedDependency), managedDependency);
        }

        for (var dependency : effectivePomInfo.dependencies()) {
            if (!scopes.contains(dependency.scope().orElse(Scope.COMPILE))) {
                continue;
            }

            var managed = dependencyManagement.get(ManagedDependencyKey.from(dependency));

            var version = dependency.version();
            if (version instanceof PomVersion.Undeclared && managed != null) {
                version = managed.version();
            }


            // TODO: Handle undeclared vs empty exclusions
            var exclusions = dependency.exclusions();
            if (exclusions.isEmpty()  && managed != null) {
                exclusions = managed.exclusions();
            }

            var group = dependency.groupId();
            var artifact = dependency.artifactId();

            // TODO: Sub in more than version from managedDependencies
            // TODO: exclusions

            if (!(group instanceof PomGroupId.Declared declaredGroup)) {
                throw new RuntimeException("Group Id is not defined");
            }

            if (!(artifact instanceof PomArtifactId.Declared declaredArtifact)) {
                throw new RuntimeException("Artifact Id is not defined");
            }

            if (!(version instanceof PomVersion.Declared declaredVersion)) {
                throw new RuntimeException("Version is not defined");
            }

            dependencies.add(
                    new Dependency(
                            new Library(
                                    new Group(declaredGroup.value()),
                                    new Artifact(declaredArtifact.value())
                            ),
                            makeCoordinate.apply(
                                    new Version(declaredVersion.value()),
                                    Exclusions.of(exclusions.stream()
                                            .map(pomExclusion -> {
                                                if (!(pomExclusion.groupId() instanceof PomGroupId.Declared declaredExclusionGroup)) {
                                                    throw new RuntimeException("Exclusion group id not declared");
                                                }
                                                if (!(pomExclusion.artifactId() instanceof PomArtifactId.Declared declaredExclusionArtifact)) {
                                                    throw new RuntimeException("Exclusion group id not declared");
                                                }
                                                return new Exclusion(
                                                        new Group(declaredExclusionGroup.value()),
                                                        new Artifact(declaredExclusionArtifact.value())
                                                );
                                            })
                                            .toList())
                            )
                    )
            );
        }
        return new PomManifest(
                List.copyOf(dependencies)
        );
    }

    private record ManagedDependencyKey(
            PomGroupId groupId,
            PomArtifactId artifactId
    ) {
        static ManagedDependencyKey from(PomDependency pomDependency) {
            return new ManagedDependencyKey(
                    pomDependency.groupId(),
                    pomDependency.artifactId()
            );
        }
    }
}
