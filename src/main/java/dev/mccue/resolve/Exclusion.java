package dev.mccue.resolve;

import dev.mccue.resolve.doc.Gold;

import java.util.Objects;

@Gold
public record Exclusion(
        Group group,
        Artifact artifact
) {
    public static final Exclusion ALL = new Exclusion(Group.ALL, Artifact.ALL);

    public Exclusion {
        Objects.requireNonNull(group, "group must not be null");
        Objects.requireNonNull(artifact, "artifact must not be null");
    }

    public Exclusion(String group, String artifact) {
        this(new Group(group), new Artifact(artifact));
    }
}
