package dev.mccue.resolve.maven;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

sealed interface PomActivation {
    enum Undeclared implements PomActivation {
        INSTANCE;


        @Override
        public String toString() {
            return "Undeclared[]";
        }
    }

    record Declared(
            Optional<JdkConstraint> jdkConstraint,
            Optional<OsConstraint> osConstraint
    ) implements PomActivation {

    }

    record JdkConstraint(String constraint) {}
    record OsConstraint(
            Optional<String> name,
            Optional<String> family,
            Optional<String> arch,
            Optional<String> version
    ) {}
}
