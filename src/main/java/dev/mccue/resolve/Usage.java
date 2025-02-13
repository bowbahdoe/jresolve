package dev.mccue.resolve;

import java.util.Objects;

/**
 * Information on how a dependency is intended to be used.
 *
 * <p>
 *     We assume that if a library's usage is unspecified that it will be used in the same way
 *     as whatever dependency brought it in.
 * </p>
 * @param value The way in which the dependency will be used.
 */
public record Usage(String value) {
    public static final Usage CLASS_PATH = new Usage("--class-path");
    public static final Usage MODULE_PATH = new Usage("--module-path");
    public static final Usage PROCESSOR_PATH = new Usage("--processor-path");
    public static final Usage PROCESSOR_MODULE_PATH = new Usage("--processor-module-path");
    public static final Usage SYSTEM_LIBRARY_PATH = new Usage("-Dsystem.library.path");

    public Usage {
        Objects.requireNonNull(value);
    }
}
