package dev.mccue.resolve;

import dev.mccue.resolve.doc.ToolsDeps;

import java.util.List;

@ToolsDeps("The word 'manifest'")
public interface Manifest {
    Manifest EMPTY = List::of;

    List<Dependency> dependencies();
}
