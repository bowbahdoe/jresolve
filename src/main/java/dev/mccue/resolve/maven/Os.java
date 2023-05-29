package dev.mccue.resolve.maven;

import dev.mccue.resolve.doc.Coursier;
import dev.mccue.resolve.doc.Maven;

import java.util.Locale;
import java.util.Objects;

/**
 * Certain build profiles take into account the current architecture
 * and operating system being used.
 *
 * <p>
 *     This can change the set of dependencies being considered.
 * </p>
 */
@Maven("""
    https://maven.apache.org/enforcer/enforcer-rules/requireOS.html
    https://maven.apache.org/guides/introduction/introduction-to-profiles.html#os
""")
@Coursier(
        "https://github.com/coursier/coursier/blob/6b2c581493011d14423827246574772d8bad663a/modules/core/shared/src/main/scala/coursier/core/Activation.scala#L59"
)
public record Os(
    String name,
    String arch,
    String version
) {
    public Os {
        Objects.requireNonNull(name);
        Objects.requireNonNull(arch);
        Objects.requireNonNull(version);
    }

    public Os() {
        this(
                System.getProperty("os.name").toLowerCase(Locale.US),
                System.getProperty("os.arch").toLowerCase(Locale.US),
                System.getProperty("os.version").toLowerCase(Locale.US)
        );
    }
}
