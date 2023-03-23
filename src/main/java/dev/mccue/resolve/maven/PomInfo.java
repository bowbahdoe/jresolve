package dev.mccue.resolve.maven;


import dev.mccue.resolve.Library;
import dev.mccue.resolve.Profile;
import dev.mccue.resolve.Scope;
import dev.mccue.resolve.Type;
import dev.mccue.resolve.doc.Coursier;
import dev.mccue.resolve.util.Tuple2;

import java.util.List;
import java.util.Optional;

@Coursier("https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/core/Definitions.scala#L228-L268")
public record PomInfo(
        Library library,
        String version,

        List<Tuple2<Scope, PomDependency>> dependencies,

        Optional<Tuple2<Library, String>> parent,

        List<Tuple2<Scope, PomDependency>> dependencyManagement,

        List<Tuple2<String, String>> properties,

        List<Profile> profiles,

        Optional<Type> packagingOpt
) {

    static List<PomDependency> smoosh(List<PomInfo> pomInfos, List<Scope> scopes)  {
        return List.of();
    }
}
