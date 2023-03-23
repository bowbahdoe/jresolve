package dev.mccue.resolve;

import dev.mccue.resolve.doc.Coursier;

import java.util.Objects;

@Coursier("https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/core/Definitions.scala#L125-L143")
public record Classifier(String value) implements Comparable<Classifier> {
    public static final Classifier EMPTY = new Classifier("");
    public static final Classifier TESTS = new Classifier("tests");
    public static final Classifier JAVADOC = new Classifier("javadoc");
    public static final Classifier SOURCES = new Classifier("sources");

    public Classifier {
        Objects.requireNonNull(value, "value must not be null");
    }

    @Override
    public int compareTo(Classifier o) {
        return this.value.compareTo(o.value);
    }
}
