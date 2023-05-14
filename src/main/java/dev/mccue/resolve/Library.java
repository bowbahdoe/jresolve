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
        @ToolsDeps(
                value = "https://clojurians.slack.com/archives/C0H28NMAS/p1680365565612789?thread_ts=1680362691.333169&cid=C0H28NMAS",
                details = "TDeps makes classifier part of the artifact group/artifact[$classifier]"
        )
        Classifier classifier
) {
    public Library {
        Objects.requireNonNull(group);
        Objects.requireNonNull(artifact);
        Objects.requireNonNull(classifier);
    }

    public Library(Group group, Artifact artifact) {
        this(group, artifact, Classifier.EMPTY);
    }

    public Library(String group, String artifact) {
        this(new Group(group), new Artifact(artifact));
    }

    @Override
    public String toString() {
        return "Library[group=" + group + ", artifact=" + artifact + (classifier.equals(Classifier.EMPTY)
                ? "]"
                : ", classifier=" + classifier + "]");
    }
}