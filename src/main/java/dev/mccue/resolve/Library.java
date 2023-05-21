package dev.mccue.resolve;

import dev.mccue.resolve.doc.ToolsDeps;

import java.util.Objects;

@ToolsDeps(
        value = "https://clojure.org/reference/dep_expansion",
        details = "tools.deps uses 'lib' as the term for this."
)
public record Library(
        Group group,
        Artifact artifact,
        Variant variant
) {
    public Library {
        Objects.requireNonNull(group);
        Objects.requireNonNull(artifact);
        Objects.requireNonNull(variant);
    }

    public Library(Group group, Artifact artifact) {
        this(group, artifact, Variant.DEFAULT);
    }

    public Library(String group, String artifact) {
        this(new Group(group), new Artifact(artifact));
    }

    @Override
    public String toString() {
        return "Library[group="
                + group
                + ", artifact="
                + artifact
                + (variant.equals(Variant.DEFAULT) ? "" : ", variant=" + variant)
                + "]";
    }
}