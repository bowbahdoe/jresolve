package dev.mccue.resolve.maven;

import dev.mccue.resolve.Group;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

sealed interface PomGroupId {
    enum Undeclared implements PomGroupId {
        INSTANCE;

        @Override
        public PomGroupId map(Function<String, String> f) {
            return this;
        }

        @Override
        public Group orElseThrow() {
            throw new RuntimeException("No group declared!");
        }

        @Override
        public void ifDeclared(Consumer<String> cb) {

        }

        @Override
        public String toString() {
            return "Undeclared[]";
        }
    }

    record Declared(String value) implements PomGroupId {
        public Declared(String value) {
            this.value = Objects.requireNonNull(value).trim();
        }

        @Override
        public PomGroupId map(Function<String, String> f) {
            return new Declared(f.apply(value));
        }

        @Override
        public Group orElseThrow() {
            return new Group(value);
        }

        @Override
        public void ifDeclared(Consumer<String> cb) {
            cb.accept(value);
        }
    }

    PomGroupId map(Function<String, String> f);

    Group orElseThrow();


    void ifDeclared(Consumer<String> cb);
}
