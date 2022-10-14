package dev.mccue.resolve.graph;

import dev.mccue.resolve.core.Module;
import dev.mccue.resolve.doc.Coursier;
import dev.mccue.resolve.doc.Incomplete;


@Incomplete
@Coursier("https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/graph/Conflict.scala")
public record Conflict(
        Module module,
        String version,
        String wantedVersion,
        boolean wasExcluded,
        Module dependeeModule,
        String dependeeVersion
) {
    public record Conflicted(ReverseModuleTree tree) {
        public Conflict conflict() {
            return new Conflict(
                    tree.dependsOnModule(),
                    tree.dependsOnReconciledVersion(),
                    tree.dependsOnVersion(),
                    tree.excludedDependsOn(),
                    tree.module(),
                    tree.reconciledVersion()
            );
        }
    }

}
