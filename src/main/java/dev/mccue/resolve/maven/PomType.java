package dev.mccue.resolve.maven;

import java.util.Objects;
import java.util.function.Function;

sealed interface PomType {
    enum Undeclared implements PomType {
        INSTANCE;

        @Override
        public PomType map(Function<String, String> f) {
            return this;
        }

        @Override
        public PomVersion.Type orElse(PomVersion.Type defaultValue) {
            return defaultValue;
        }

        @Override
        public String toString() {
            return "Undeclared[]";
        }
    }

    record Declared(String value) implements PomType {
        public Declared(String value) {
            this.value = Objects.requireNonNull(value).trim();
        }

        @Override
        public PomType map(Function<String, String> f) {
            return new Declared(f.apply(value));
        }

        @Override
        public PomVersion.Type orElse(PomVersion.Type defaultValue) {
            return new PomVersion.Type(value);
        }
    }

    PomType map(Function<String, String> f);

    PomVersion.Type orElse(PomVersion.Type defaultValue);
}
