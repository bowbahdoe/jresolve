package dev.mccue.resolve;

import dev.mccue.resolve.doc.Coursier;

import java.util.*;
import java.util.function.Function;

@Coursier("https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/core/Definitions.scala#L18-L26")
public record Artifact(String value) implements Comparable<Artifact> {
    public static final Artifact ALL = new Artifact("*");
    public Artifact {
        Objects.requireNonNull(value, "value must not be null");
    }

    Artifact map(Function<String, String> f) {
        return new Artifact(f.apply(this.value));
    }

    @Override
    public int compareTo(Artifact o) {
        return this.value.compareTo(o.value);
    }

    @Override
    public String toString() {
        return value;
    }

}
