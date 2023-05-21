package dev.mccue.resolve.maven;

import java.util.Objects;
import java.util.function.Function;

sealed interface PomClassifier {
    enum Undeclared implements PomClassifier {
        INSTANCE;

        @Override
        public PomClassifier map(Function<String, String> f) {
            return this;
        }

        @Override
        public Classifier orElse(Classifier defaultValue) {
            return defaultValue;
        }

        @Override
        public String toString() {
            return "Undeclared[]";
        }
    }

    record Declared(String value) implements PomClassifier {
        public Declared(String value) {
            this.value = Objects.requireNonNull(value).trim();
        }

        @Override
        public PomClassifier map(Function<String, String> f) {
            return new Declared(f.apply(value));
        }

        @Override
        public Classifier orElse(Classifier defaultValue) {
            return new Classifier(value);
        }
    }

    PomClassifier map(Function<String, String> f);

    Classifier orElse(Classifier defaultValue);
}