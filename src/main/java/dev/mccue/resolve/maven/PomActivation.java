package dev.mccue.resolve.maven;


import dev.mccue.resolve.Version;
import dev.mccue.resolve.VersionRange;
import dev.mccue.resolve.doc.Coursier;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Coursier("https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/core/Activation.scala")
record PomActivation(
        List<PomProperty> properties,
        Os os,
        Jdk jdk
) {
    sealed interface Jdk {
        record Unspecified() implements Jdk {}
        record Interval(VersionRange versionInterval) implements Jdk {}
        record SpecificVersions(List<Version> versions) implements Jdk {}
    }
    record Os(
            Optional<String> arch,
            Set<String> families,
            Optional<String> name,
            Optional<String> version
    ) {}
}