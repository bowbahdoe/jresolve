package dev.mccue.resolve;

import dev.mccue.resolve.doc.Coursier;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

@Coursier("https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/core/Definitions.scala#L8-L9")
public record Group(String value) implements Comparable<Group> {
    public static Group ALL = new Group("*");
    public Group {
        Objects.requireNonNull(value, "value must not be null");
    }

    Group map(Function<String, String> f) {
        return new Group(f.apply(this.value));
    }

    List<String> explode() {
        return Arrays.asList(value.split("\\."));
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
