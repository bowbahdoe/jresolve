package dev.mccue.resolve.core;

import dev.mccue.resolve.doc.Coursier;
import dev.mccue.resolve.doc.Incomplete;

@Incomplete
@Coursier("https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/core/VersionInterval.scala")
public record VersionInterval(
        From from,
        To to,
        boolean fromIncluded,
        boolean toIncluded
) {
    sealed interface From {
    }

    sealed interface To {
    }

    record NotSpecified() implements From, To{}
    record Specified(Version version) implements From, To {}
}
