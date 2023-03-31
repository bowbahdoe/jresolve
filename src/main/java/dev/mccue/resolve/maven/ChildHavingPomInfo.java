package dev.mccue.resolve.maven;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Pom info where the parents of a POM have been downloaded
 * and included into a linked-list.
 */
record ChildHavingPomInfo(
        PomGroupId groupId,
        PomArtifactId artifactId,
        PomVersion version,
        List<PomDependency> dependencies,
        List<PomDependency> dependencyManagement,
        List<PomProperty> properties,
        PomPackaging packaging,
        Optional<ChildHavingPomInfo> child
) {

    void printPath(Function<ChildHavingPomInfo, ?> f) {
        var top = this;

        while (top != null) {
            System.out.print(f.apply(top));
            if (top.child.isPresent()) {
                System.out.print(" -> ");
            }
            top = top.child().orElse(null);
        }

        System.out.println();
    }
}
