package dev.mccue.resolve;

import dev.mccue.resolve.maven.Classifier;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

public final class Fetch {
    private final Resolve resolve;
    private final LinkedHashSet<Classifier> classifiers;
    private boolean includeSources;
    private boolean includeDocumentation;

    public Fetch() {
        this.resolve = new Resolve();
        this.classifiers = new LinkedHashSet<>();
        this.includeSources = false;
        this.includeDocumentation = false;
    }

    public Fetch addDependency(Dependency dependency) {
        this.resolve.addDependency(dependency);
        return this;
    }

    public Fetch addDependencies(List<Dependency> dependencies) {
        this.resolve.addDependencies(dependencies);
        return this;
    }


    public Fetch withCache(Cache cache) {
        this.resolve.withCache(cache);
        return this;
    }

    public Fetch addDependencyOverride(Dependency dependency) {
        this.resolve.addDependencyOverride(dependency);
        return this;
    }

    public Fetch addDependencyOverrides(List<Dependency> dependencies) {
        this.resolve.addDependencyOverrides(dependencies);
        return this;
    }

    public Fetch includeSources(boolean includeSources) {
        this.includeSources = includeSources;
        return this;
    }


    public Fetch includeSources() {
        return includeSources(true);
    }

    public Fetch includeDocumentation(boolean includeDocumentation) {
        this.includeDocumentation = includeDocumentation;
        return this;
    }

    public Fetch includeDocumentation() {
        return includeDocumentation(true);
    }

    public Result run() {
        var selectedDependencies = this.resolve.run().selectedDependencies();
        var libraries = selectedDependencies
                .stream()
                .map(dependency -> dependency.coordinate().getLibraryLocation(dependency.library(), resolve.cache))
                .toList();

        List<Path> sources = this.includeSources
                ? selectedDependencies.stream()
                        .flatMap(dependency -> dependency.coordinate()
                                .getLibrarySourcesLocation(dependency.library(), resolve.cache)
                                .stream())
                        .toList()
                : List.of();

        List<Path> documentation = this.includeDocumentation
                ? selectedDependencies.stream()
                        .flatMap(dependency -> dependency.coordinate()
                        .getLibraryDocumentationLocation(dependency.library(), resolve.cache)
                                    .stream())
                            .toList()
                : List.of();

        return new Result(libraries, sources, documentation);
    }

    public record Result(
            List<Path> libraries,
            List<Path> sources,
            List<Path> documentation
    ) {
        public Result {
            Objects.requireNonNull(libraries);
            Objects.requireNonNull(sources);
            Objects.requireNonNull(documentation);
        }

        public String classpath() {
            return libraries.stream().map(Path::toString)
                    .collect(Collectors.joining(":"));
        }
    }
}
