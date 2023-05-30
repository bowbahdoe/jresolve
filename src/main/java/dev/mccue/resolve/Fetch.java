package dev.mccue.resolve;

import java.io.File;
import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Fetch {
    private final Supplier<Resolution> resolutionSupplier;
    private Cache cache;
    private boolean includeSources;
    private boolean includeDocumentation;
    private ExecutorService executorService;

    public Fetch(Resolve resolve) {
        this.resolutionSupplier = resolve::run;
        this.executorService = Executors.newThreadPerTaskExecutor(
                Thread.ofVirtual()
                        .name("fetch-", 0)
                        .factory()
        );
        this.cache = resolve.cache;
        this.includeSources = false;
        this.includeDocumentation = false;
    }

    public Fetch(Resolution resolution) {
        this.resolutionSupplier = () -> resolution;
        this.executorService = Executors.newThreadPerTaskExecutor(
                Thread.ofVirtual()
                        .name("fetch-", 0)
                        .factory()
        );
        this.cache = Cache.standard();
        this.includeSources = false;
        this.includeDocumentation = false;
    }

    public Fetch withCache(Cache cache) {
        this.cache = cache;
        return this;
    }

    public Fetch withExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
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

        Map<Library, Future<Path>> futurePaths = selectedDependencies.stream()
                .collect(Collectors.toUnmodifiableMap(
                        Dependency::library,
                        dependency -> this.executorService.submit(() ->
                                dependency.coordinate().getLibraryLocation(this.cache)
                        )
                ));

        Map<Library, Path> libraries = futurePaths
                .entrySet()
                .stream()
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        entry -> {
                            try {
                                return entry.getValue().get();
                            } catch (InterruptedException | ExecutionException e) {
                                throw new RuntimeException(e);
                            }
                        }
                ));

        Map<Library, Future<Optional<Path>>> futureSources = this.includeSources
                ? selectedDependencies.stream()
                .collect(Collectors.toUnmodifiableMap(
                        Dependency::library,
                        dependency -> this.executorService.submit(() ->
                                dependency.coordinate().getLibrarySourcesLocation(this.cache)
                        )
                ))
                : Map.of();

        Map<Library, Path> sources = futureSources
                .entrySet()
                .stream()
                .map(entry -> {
                    try {
                        return Map.entry(entry.getKey(), entry.getValue().get());
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }

                })
                .filter(entry -> entry.getValue().isPresent())
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().orElseThrow()
                ));

        Map<Library, Future<Optional<Path>>> futureDocumentation = this.includeSources
                ? selectedDependencies.stream()
                .collect(Collectors.toUnmodifiableMap(
                        Dependency::library,
                        dependency -> this.executorService.submit(() ->
                                dependency.coordinate().getLibraryDocumentationLocation(
                                        this.cache
                                )
                        )
                ))
                : Map.of();

        Map<Library, Path> documentation = futureDocumentation
                .entrySet()
                .stream()
                .map(entry -> {
                    try {
                        return Map.entry(entry.getKey(), entry.getValue().get());
                    } catch (InterruptedException | ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                })
                .filter(entry -> entry.getValue().isPresent())
                .collect(Collectors.toUnmodifiableMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().orElseThrow()
                ));


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
