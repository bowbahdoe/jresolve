package dev.mccue.resolve.core;

import dev.mccue.resolve.doc.Coursier;

import java.util.Objects;
import java.util.function.Function;

@Coursier("https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/core/Definitions.scala#L145-L163")
public record Extension(String value) implements Comparable<Extension> {
    public static final Extension JAR = new Extension("jar");
    public static final Extension POM = new Extension("pom");
    public static final Extension EMPTY = new Extension("");

    public Extension {
        Objects.requireNonNull(value);
    }

    public boolean isEmpty() {
        return this.value.isEmpty();
    }

    public Extension map(Function<String, String> f) {
        return new Extension(f.apply(this.value));
    }

    public Type asType() {
        return new Type(this.value);
    }

    @Override
    public int compareTo(Extension extension) {
        return this.value.compareTo(extension.value);
    }
}
