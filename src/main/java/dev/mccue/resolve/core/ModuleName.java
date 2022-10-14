package dev.mccue.resolve.core;

import dev.mccue.resolve.doc.Coursier;

import java.util.Objects;
import java.util.function.Function;

@Coursier("https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/core/Definitions.scala#L18-L26")
public record ModuleName(String value) implements Comparable<ModuleName> {
    public static ModuleName ALL = new ModuleName("*");
    public ModuleName {
        Objects.requireNonNull(value, "value must not be null");
    }

    public ModuleName map(Function<String, String> f) {
        return new ModuleName(f.apply(this.value));
    }

    @Override
    public int compareTo(ModuleName o) {
        return this.value.compareTo(o.value);
    }
}
