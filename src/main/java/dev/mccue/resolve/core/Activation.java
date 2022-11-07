package dev.mccue.resolve.core;

import dev.mccue.resolve.doc.Coursier;
import dev.mccue.resolve.doc.Incomplete;
import dev.mccue.resolve.util.Tuple2;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.http.HttpClient;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Incomplete
@Coursier("https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/core/Activation.scala")
public record Activation(
        List<Tuple2<String, Optional<String>>> properties,
        Os os,
        Jdk jdk
) {
    public sealed interface Jdk {
        record Unspecified() implements Jdk {}
        record Interval(VersionInterval versionInterval) implements Jdk {}
        record SpecificVersions(List<Version> versions) implements Jdk {}
    }
    public record Os(
            Optional<String> arch,
            Set<String> families,
            Optional<String> name,
            Optional<String> version
    ) {}
}
