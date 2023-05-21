package dev.mccue.resolve.maven;

import java.util.Objects;
import java.util.function.Function;

sealed interface PomOptionality {
    enum Undeclared implements PomOptionality {
        INSTANCE;

        @Override
        public PomOptionality map(Function<String, String> f) {
            return this;
        }

        @Override
        public String orElse(String defaultValue) {
            return defaultValue;
        }

        @Override
        public String toString() {
            return "Undeclared[]";
        }
    }

    record Declared(String value) implements PomOptionality {
        public Declared(String value) {
            this.value = Objects.requireNonNull(value).trim();
        }

        @Override
        public PomOptionality map(Function<String, String> f) {
            return new Declared(f.apply(value));
        }

        @Override
        public String orElse(String defaultValue) {
            return value;
        }
    }

    PomOptionality map(Function<String, String> f);

    String orElse(String defaultValue);
}
