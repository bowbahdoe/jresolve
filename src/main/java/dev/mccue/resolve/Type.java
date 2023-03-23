package dev.mccue.resolve;


import dev.mccue.resolve.doc.Coursier;

import java.util.Objects;

@Coursier("https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/core/Definitions.scala#L81-L123")
public record Type(String value) implements Comparable<Type> {
    public static final Type JAR = new Type("jar");
    public static final Type TEST_JAR = new Type("test-jar");
    public static final Type BUNDLE = new Type("bundle");
    public static final Type DOC = new Type("doc");
    public static final Type SOURCE = new Type("src");

    public static final Type JAVADOC = new Type("javadoc");

    public static final Type JAVA_SOURCE = new Type("java-source");

    public static final Type IVY = new Type("ivy");
    public static final Type POM = new Type("pom");
    public static final Type EMPTY = new Type("");
    public static final Type ALL = new Type("*");

    public Type {
        Objects.requireNonNull(value, "value must not be null");
    }

    @Override
    public int compareTo(Type type) {
        return this.value.compareTo(type.value);
    }
}