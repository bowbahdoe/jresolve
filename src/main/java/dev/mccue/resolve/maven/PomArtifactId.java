package dev.mccue.resolve.maven;

import dev.mccue.resolve.Artifact;
import dev.mccue.resolve.Group;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

sealed interface PomArtifactId {
    enum Undeclared implements PomArtifactId {
        INSTANCE;

        @Override
        public Artifact orElseThrow() {
            throw new RuntimeException("No artifact declared");
        }

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
        public Artifact orElseThrow() {
            return new Artifact(value);
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


    Artifact orElseThrow();

    PomArtifactId map(Function<String, String> f);

    void ifDeclared(Consumer<String> cb);
}