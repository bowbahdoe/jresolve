package dev.mccue.resolve.maven;

import java.util.Objects;
import java.util.function.Function;

sealed interface PomPackaging {
    enum Undeclared implements PomPackaging {
        INSTANCE;

        @Override
        public PomPackaging map(Function<String, String> f) {
            return this;
        }

        @Override
        public String toString() {
            return "Undeclared[]";
        }
    }

    record Declared(String value) implements PomPackaging {
        public Declared(String value) {
            this.value = Objects.requireNonNull(value).trim();
        }

        @Override
        public PomPackaging map(Function<String, String> f) {
            return new Declared(f.apply(value));
        }
    }

    PomPackaging map(Function<String, String> f);
}
