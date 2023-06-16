package dev.mccue.resolve.maven;

import dev.mccue.resolve.Cache;
import dev.mccue.resolve.Library;
import dev.mccue.resolve.doc.Rife;
import org.jspecify.annotations.NullMarked;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Represents information derived from a POM after all information
 * from Parent poms is merged in and property values have been substituted.
 */
@NullMarked
record EffectivePomInfo(
        PomGroupId groupId,
        PomArtifactId artifactId,
        PomVersion version,
        List<PomDependency> dependencies,
        List<PomDependency> dependencyManagement,
        PomPackaging packaging,
        Map<String, String> properties
) {

    static EffectivePomInfo from(
            final ChildHavingPomInfo childHavingPomInfo,
            final Runtime.Version jdkVersion,
            final Os os
    ) {
        var properties = new LinkedHashMap<String, String>();

        var top = childHavingPomInfo;
        while (top != null) {
            for (PomProperty property : top.properties()) {
                properties.put(property.key(), property.value());
            }
            top = top.child().orElse(null);
        }

        properties.put("java.version", jdkVersion.toString());
        properties.put("os.name", os.name());
        properties.put("os.arch", os.arch());
        properties.put("os.version", os.version());

        Function<String, String> resolve =
                str -> resolveProperties(properties, str);

        Function<PomDependency, PomDependency> resolveDep = dependency ->
                dependency.map(resolve);

        PomGroupId groupId = PomGroupId.Undeclared.INSTANCE;
        PomVersion version = PomVersion.Undeclared.INSTANCE;
        PomPackaging packaging = PomPackaging.Undeclared.INSTANCE;
        var dependencies = new LinkedHashMap<PomDependencyKey, PomDependency>();
        var dependencyManagement = new LinkedHashMap<PomDependencyKey, PomDependency>();

        top = childHavingPomInfo;
        while (top != null) {
            if (top.groupId() instanceof PomGroupId.Declared) {
                groupId = top.groupId().map(resolve);
            }
            if (top.version() instanceof PomVersion.Declared) {
                version = top.version().map(resolve);
            }
            if (top.packaging() instanceof PomPackaging.Declared) {
                packaging = top.packaging().map(resolve);
            }

            top = top.child().orElse(null);
        }

        groupId.ifDeclared(value -> properties.put("project.groupId", value));
        version.ifDeclared(value -> properties.put("project.version", value));


        var artifactId = childHavingPomInfo.artifactId().map(resolve);
        artifactId.ifDeclared(value -> properties.put("project.artifactId", value));

        top = childHavingPomInfo;
        while (top != null) {

            top.dependencies()
                    .forEach(dependency -> {
                        var newDep = resolveDep.apply(dependency);
                        dependencies.put(PomDependencyKey.from(newDep), newDep);
                    });

            top.dependencyManagement()
                    .forEach(dependency -> {
                        var newDep = resolveDep.apply(dependency);
                        dependencyManagement.put(PomDependencyKey.from(newDep), newDep);
                    });

            top = top.child().orElse(null);
        }

        return new EffectivePomInfo(
                groupId,
                artifactId,
                version,
                List.copyOf(dependencies.values()),
                List.copyOf(dependencyManagement.values()),
                packaging,
                properties
        );
    }

    EffectivePomInfo resolveImports(MavenRepository repository, Cache cache, Runtime.Version jdkVersion, Os os) {
        var props = new LinkedHashMap<>(properties);
        var dependencyManagementWithImportsFlattened = this.dependencyManagement.stream()
                .mapMulti((PomDependency dependency, Consumer<PomDependency> addDep) -> {
                    if (!dependency.scope().orElse(Scope.COMPILE).equals(Scope.IMPORT)) {
                        addDep.accept(dependency);
                    }
                    else {
                        var effectiveBom = EffectivePomInfo.from(repository.getAllPoms(
                                        dependency.groupId().orElseThrow(),
                                        dependency.artifactId().orElseThrow(),
                                        dependency.version().orElseThrow(),
                                        cache
                                ), jdkVersion, os)
                                .resolveImports(repository, cache, jdkVersion, os);
                        effectiveBom.properties.forEach((k, v) -> {
                            if (!props.containsKey(k)) {
                                props.put(k, v);
                            }
                        });
                        props.putAll(effectiveBom.properties);
                        effectiveBom.dependencyManagement.forEach(addDep);
                    }
                })
                .toList();
        return new EffectivePomInfo(
                groupId,
                artifactId,
                version,
                dependencies
                        .stream()
                        .map(dep -> dep.map(s -> resolveProperties(props, s)))
                        .toList(),
                dependencyManagementWithImportsFlattened
                        .stream()
                        .map(dep -> dep.map(s -> resolveProperties(props, s)))
                        .toList(),
                packaging,
                props
        );
    }
    /*
    This is because the minimal set of information for matching a dependency reference against a dependencyManagement section is actually {groupId, artifactId, type, classifier}.
    https://maven.apache.org/guides/introduction/introduction-to-dependency-mechanism.html
     */
    private record PomDependencyKey(
            PomGroupId groupId,
            PomArtifactId artifactId,
            PomVersion version,
            PomClassifier classifier,
            PomType type
    ) {
        static PomDependencyKey from(PomDependency pomDependency) {
            return new PomDependencyKey(
                    pomDependency.groupId(),
                    pomDependency.artifactId(),
                    pomDependency.version(),
                    pomDependency.classifier(),
                    pomDependency.type()
            );
        }
    }

    private static final Pattern MAVEN_PROPERTY = Pattern.compile("\\$\\{([^<>{}]+)}");

    @Rife("")
    static String resolveProperties(Map<String, String> properties, String data) {
        if (data == null) {
            return null;
        }

        var processed_data = new StringBuilder();
        var matcher = MAVEN_PROPERTY.matcher(data);
        var last_end = 0;
        while (matcher.find()) {
            if (matcher.groupCount() == 1) {
                var property = matcher.group(1);
                if (properties.containsKey(property)) {
                    processed_data.append(data, last_end, matcher.start());
                    processed_data.append(properties.get(property));
                    last_end = matcher.end();
                }
            }
        }
        if (last_end < data.length()) {
            processed_data.append(data.substring(last_end));
        }

        return processed_data.toString();
    }
}
