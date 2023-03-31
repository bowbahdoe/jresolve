package dev.mccue.resolve.maven;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

sealed interface PomArtifactId {
    enum Undeclared implements PomArtifactId {
        INSTANCE;

        @Override
        public PomArtifactId map(Function<String, String> f) {
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

    record Declared(String value) implements PomArtifactId {
        public Declared(String value) {
            this.value = Objects.requireNonNull(value).trim();
        }

        @Override
        public PomArtifactId map(Function<String, String> f) {
            return new Declared(f.apply(value));
        }

        @Override
        public void ifDeclared(Consumer<String> cb) {
            cb.accept(value);
        }
    }

    PomArtifactId map(Function<String, String> f);

    void ifDeclared(Consumer<String> cb);
}