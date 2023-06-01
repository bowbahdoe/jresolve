package dev.mccue.resolve.maven;


import dev.mccue.resolve.doc.Coursier;

import java.util.List;

@Coursier("https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/core/Definitions.scala#L228-L268")
record PomInfo(
        PomGroupId groupId,
        PomArtifactId artifactId,
        PomVersion version,

        List<PomDependency> dependencies,

        PomParent parent,

        List<PomDependency> dependencyManagement,

        List<PomProperty> properties,

        PomPackaging packaging,
        List<PomProfile> profiles
) {
}
