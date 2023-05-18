package dev.mccue.resolve;

import dev.mccue.resolve.doc.ToolsDeps;

import java.util.List;

@ToolsDeps("The word 'manifest'")
public interface Manifest {
    Manifest EMPTY = List::of;

    static Manifest of(List<? extends Dependency> dependencies) {
        return () -> List.copyOf(dependencies);
    }

    List<Dependency> dependencies();

}
