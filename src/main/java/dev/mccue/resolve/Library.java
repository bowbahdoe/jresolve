package dev.mccue.resolve;

import dev.mccue.resolve.doc.ToolsDeps;

@ToolsDeps(
        value = "https://clojure.org/reference/dep_expansion",
        details = "tools.deps uses 'lib' as the term for this."
)
public record Library(
        Group group,
        Artifact artifact
) {
    public Library(String group, String artifact) {
        this(new Group(group), new Artifact(artifact));
    }

    @Override
    public String toString() {
        return group + "/" + artifact;
    }
}