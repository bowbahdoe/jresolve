package dev.mccue.resolve.maven;

import dev.mccue.resolve.Variant;
import dev.mccue.resolve.doc.Coursier;

import java.util.Objects;
import java.util.function.Function;

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

    public Classifier map(Function<String, String> f) {
        return new Classifier(f.apply(value));
    }

    public Variant asVariant() {
        return this == EMPTY ? Variant.DEFAULT : new Variant(value);
    }

    @Override
    public String toString() {
        if (this.equals(Classifier.EMPTY)) {
            return "Classifier[EMPTY]";
        }
        else {
            return "Classifier[" +
                    "value='" + value + '\'' +
                    ']';
        }
    }
}
