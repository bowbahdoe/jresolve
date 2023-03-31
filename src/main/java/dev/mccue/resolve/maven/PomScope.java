package dev.mccue.resolve.maven;

import java.util.Objects;
import java.util.function.Function;

sealed interface PomScope {
    enum Undeclared implements PomScope {
        INSTANCE;

        @Override
        public PomScope map(Function<String, String> f) {
            return this;
        }

        @Override
        public Scope orElse(Scope defaultValue) {
            return defaultValue;
        }

        @Override
        public String toString() {
            return "Undeclared[]";
        }
    }

    record Declared(String value) implements PomScope {
        public Declared(String value) {
            this.value = Objects.requireNonNull(value).trim();
        }

        @Override
        public PomScope map(Function<String, String> f) {
            return new Declared(f.apply(value));
        }

        @Override
        public Scope orElse(Scope defaultValue) {
            return new Scope(value);
        }
    }

    PomScope map(Function<String, String> f);

    Scope orElse(Scope defaultValue);
}
