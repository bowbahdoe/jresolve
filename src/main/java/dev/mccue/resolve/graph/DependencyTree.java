package dev.mccue.resolve.graph;

import dev.mccue.resolve.core.Dependency;
import dev.mccue.resolve.doc.Coursier;
import dev.mccue.resolve.doc.Incomplete;

import java.util.List;

@Incomplete
@Coursier("https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/graph/DependencyTree.scala")
public interface DependencyTree {
    Dependency dependency();

    boolean excluded();

    String reconciledVersion();
    String retainedVersion();

    List<DependencyTree> children();
}
