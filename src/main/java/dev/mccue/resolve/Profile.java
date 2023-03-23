package dev.mccue.resolve;

import dev.mccue.resolve.doc.Coursier;
import dev.mccue.resolve.maven.PomDependency;
import dev.mccue.resolve.util.Tuple2;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Coursier("https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/core/Definitions.scala#L348-L356")
public record Profile(
        String id,
        Optional<Boolean> activeByDefault,
        List<Tuple2<Scope, PomDependency>> dependencies,
        List<Tuple2<Scope, PomDependency>> dependencyManagement,
        Map<String, String> properties
) {
}
