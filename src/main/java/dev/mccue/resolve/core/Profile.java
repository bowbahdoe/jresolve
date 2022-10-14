package dev.mccue.resolve.core;

import dev.mccue.resolve.doc.Coursier;
import dev.mccue.resolve.doc.Incomplete;
import dev.mccue.resolve.doc.MavenSpecific;
import dev.mccue.resolve.util.Tuple2;

import java.util.List;
import java.util.Map;
import java.util.Optional;


@Incomplete
@Coursier("https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/core/Definitions.scala#L348-L356")
@MavenSpecific
public record Profile(
        String id,
        Optional<Boolean> activeByDefault,
        Activation activation,
        List<Tuple2<Configuration, Dependency>> dependencies,
        List<Tuple2<Configuration, Dependency>> dependencyManagement,
        Map<String, String> properties
) {
}
