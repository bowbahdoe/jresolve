package dev.mccue.resolve.maven;

import dev.mccue.resolve.doc.Coursier;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

sealed interface PomVersion {
    enum Undeclared implements PomVersion {
        INSTANCE;

        @Override
        public PomVersion map(Function<String, String> f) {
            return this;
        }

        @Override
        public void ifDeclared(Consumer<String> cb) {

        }

        @Override
        public String toString() {
            return "Undeclared[]";
        }
    }

    record Declared(String value) implements PomVersion {
        public Declared(String value) {
            this.value = Objects.requireNonNull(value).trim();
        }

        @Override
        public PomVersion map(Function<String, String> f) {
            return new Declared(f.apply(value));
        }

        @Override
        public void ifDeclared(Consumer<String> cb) {
            cb.accept(value);
        }
    }

    PomVersion map(Function<String, String> f);

    void ifDeclared(Consumer<String> cb);

    @Coursier("https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/core/Definitions.scala#L81-L123")
    record Type(String value) implements Comparable<Type> {
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

        public Type map(Function<String, String> f) {
            return new Type(f.apply(value));
        }

        @Override
        public String toString() {
            if (this.equals(Type.EMPTY)) {
                return "Type[EMPTY]";
            }
            else {
                return "Type[" +
                        "value='" + value + '\'' +
                        ']';
            }
        }
    }
}
