package dev.mccue.resolve.maven;

import dev.mccue.resolve.Version;
import dev.mccue.resolve.doc.Coursier;
import org.jspecify.annotations.NullMarked;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

@NullMarked
sealed interface PomVersion {
    enum Undeclared implements PomVersion {
        INSTANCE;

        @Override
        public Version orElseThrow() {
            throw new RuntimeException("Version not declared");
        }

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
        public Version orElseThrow() {
            return new Version(value);
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

    Version orElseThrow();
    PomVersion map(Function<String, String> f);

    void ifDeclared(Consumer<String> cb);

}
