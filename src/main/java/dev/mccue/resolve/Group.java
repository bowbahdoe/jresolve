package dev.mccue.resolve;

import dev.mccue.resolve.doc.Coursier;

import java.util.Objects;
import java.util.function.Function;

@Coursier("https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/core/Definitions.scala#L8-L9")
public record Group(String value) implements Comparable<Group> {
    public static Group ALL = new Group("*");
    public Group {
        Objects.requireNonNull(value, "value must not be null");
    }

    public Group map(Function<String, String> f) {
        return new Group(f.apply(this.value));
    }

    @Override
    public int compareTo(Group o) {
        return this.value.compareTo(o.value);
    }

    @Override
    public String toString() {
        return value;
    }
}
