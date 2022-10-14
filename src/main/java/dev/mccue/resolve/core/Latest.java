package dev.mccue.resolve.core;

import dev.mccue.resolve.doc.Coursier;

import java.util.Optional;

@Coursier("https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/core/Latest.scala")
public enum Latest {
    INTEGRATION("integration"),
    RELEASE("release"),
    STABLE("stable");

    private final String value;

    public String value() {
        return this.value;
    }

    Latest(String value) {
        this.value = value;
    }

    public static Optional<Latest> from(String s) {
        return switch (s) {
            case "latest.integration" -> Optional.of(INTEGRATION);
            case "latest.release" -> Optional.of(RELEASE);
            case "latest.stable" -> Optional.of(STABLE);
            default -> Optional.empty();
        };
    }
}
