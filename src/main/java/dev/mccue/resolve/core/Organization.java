package dev.mccue.resolve.core;

import dev.mccue.resolve.doc.Coursier;

import java.util.Objects;
import java.util.function.Function;

@Coursier("https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/core/Definitions.scala#L8-L9")
public record Organization(String value) implements Comparable<Organization> {
    public static Organization ALL = new Organization("*");
    public Organization {
        Objects.requireNonNull(value, "value must not be null");
    }

    public Organization map(Function<String, String> f) {
        return new Organization(f.apply(this.value));
    }

    @Override
    public int compareTo(Organization o) {
        return this.value.compareTo(o.value);
    }
}
