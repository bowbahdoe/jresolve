package dev.mccue.resolve.maven;

import dev.mccue.resolve.doc.Coursier;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;
import java.util.function.Function;

@NullMarked
@Coursier("https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/core/Definitions.scala#L165-L197")
public record Scope(String value) implements Comparable<Scope> {
    public static final Scope COMPILE = new Scope("compile");
    public static final Scope RUNTIME = new Scope("runtime");
    public static final Scope TEST = new Scope("test");
    public static final Scope DEFAULT = new Scope("default");
    public static final Scope DEFAULT_COMPILE = new Scope("default(compile)");
    public static final Scope PROVIDED = new Scope("provided");
    public static final Scope IMPORT = new Scope("import");
    public static final Scope OPTIONAL = new Scope("optional");
    public static final Scope ALL = new Scope("*");

    public Scope {
        Objects.requireNonNull(value, "value must not be null");
    }

    public Scope map(Function<String, String> f) {
        return new Scope(f.apply(this.value));
    }

    @Override
    public int compareTo(Scope o) {
        return this.value.compareTo(o.value);
    }
}