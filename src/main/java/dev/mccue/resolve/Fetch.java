package dev.mccue.resolve;

import java.io.File;
import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Fetch {
    private final Supplier<Resolution> resolutionSupplier;
    private Cache cache;
    private boolean includeSources;
    private boolean includeDocumentation;

    public Fetch(Resolve resolve) {
        this.resolutionSupplier = resolve::run;
        this.cache = resolve.cache;
        this.includeSources = false;
        this.includeDocumentation = false;
    }

    public Fetch(Resolution resolution) {
        this.resolutionSupplier = () -> resolution;
        this.cache = Cache.standard();
        this.includeSources = false;
        this.includeDocumentation = false;
    }

    public Fetch withCache(Cache cache) {
        this.cache = cache;
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
        var selectedDependencies = resolutionSupplier.get().selectedDependencies();
        Map<Library, Path> libraries = selectedDependencies
                .stream()
                .collect(Collectors.toUnmodifiableMap(
                        Dependency::library,
                        dependency -> dependency.coordinate().getLibraryLocation(dependency.library(), this.cache)
                ));

        Map<Library, Path> sources = this.includeSources
                ? selectedDependencies.stream()
                .<Map.Entry<Library, Path>>mapMulti((dependency, consumer) ->
                        dependency
                                .coordinate()
                                .getLibrarySourcesLocation(dependency.library(), this.cache)
                                .ifPresent(path -> consumer.accept(Map.entry(dependency.library(), path)))
                )
                        .collect(Collectors.toUnmodifiableMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue
                        ))
                : Map.of();

        Map<Library, Path> documentation = this.includeDocumentation
                ? selectedDependencies.stream()
                .<Map.Entry<Library, Path>>mapMulti((dependency, consumer) ->
                        dependency
                                .coordinate()
                                .getLibraryDocumentationLocation(dependency.library(), this.cache)
                                .ifPresent(path -> consumer.accept(Map.entry(dependency.library(), path)))
                )
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue
                ))
                : Map.of();

        return new Result(libraries, sources, documentation);
    }

    public record Result(
            Map<Library, Path> libraries,
            Map<Library, Path> sources,
            Map<Library, Path> documentation
    ) {
        public Result {
            Objects.requireNonNull(libraries);
            Objects.requireNonNull(sources);
            Objects.requireNonNull(documentation);
        }

        public String path(List<Path> extraPaths) {
            return Stream.concat(
                    libraries.values().stream().map(Path::toString),
                    extraPaths.stream().map(Path::toString)
            ).collect(Collectors.joining(File.pathSeparator));
        }

        public String path() {
            return path(List.of());
        }

        public record Paths(
                String modulePath,
                String classPath
        ) {
            public Paths {
                Objects.requireNonNull(modulePath);
                Objects.requireNonNull(classPath);
            }
        }

        public Paths paths(
                Predicate<Library> shouldGoOnClassPath
        ) {
            return paths(shouldGoOnClassPath, List.of(), List.of());
        }

        public Paths paths(
                Predicate<Library> shouldGoOnClassPath,
                List<Path> extraClassPaths,
                List<Path> extraModulePaths
        ) {
            return new Paths(
                    Stream.concat(
                                    libraries.entrySet()
                                            .stream()
                                            .filter(entry -> !shouldGoOnClassPath.test(entry.getKey()))
                                            .map(Map.Entry::getValue)
                                            .map(Path::toString),
                                    extraModulePaths.stream().map(Path::toString)
                            )
                            .collect(Collectors.joining(File.pathSeparator)),

                    Stream.concat(
                                libraries.entrySet()
                                    .stream()
                                    .filter(entry -> shouldGoOnClassPath.test(entry.getKey()))
                                    .map(Map.Entry::getValue)
                                    .map(Path::toString),
                                extraClassPaths.stream().map(Path::toString)
                            )
                            .collect(Collectors.joining(File.pathSeparator))
            );
        }

        public ModuleFinder moduleFinder() {
            return ModuleFinder.of(libraries.values().toArray(Path[]::new));
        }
    }
}
