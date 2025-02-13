package dev.mccue.resolve;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@NullMarked
public final class Fetch {
    private final Supplier<? extends Resolve.@Nullable Result> resolutionSupplier;
    private final List<Dependency> dependencies;
    private @Nullable Cache cache;
    private boolean includeLibraries;
    private boolean includeSources;
    private boolean includeDocumentation;
    private ExecutorService executorService;

    public Fetch(Resolve resolve) {
        this(resolve::run, List.of(), resolve.cache);
    }

    public Fetch(Resolve.Result result) {
        this(() -> result, List.of(), Cache.standard());
    }

    /**
     * An explicit list of dependencies to fetch. Their manifests are ignored.
     * @param dependencies The list of deps.
     */
    public Fetch(List<Dependency> dependencies) {
        this(() -> null, dependencies, Cache.standard());
    }

    private Fetch(Supplier<Resolve.Result> resolutionSupplier, List<Dependency> dependencies, Cache cache) {
        this.resolutionSupplier = resolutionSupplier;
        this.dependencies = List.copyOf(dependencies);
        var count = new AtomicInteger();
        this.executorService = Executors.newFixedThreadPool(8, (r) -> {
            var t = new Thread(r);
            t.setName("fetch-" + count.getAndIncrement());
            t.setDaemon(true);
            return t;
        });
        this.cache = cache;
        this.includeLibraries = true;
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

    public Fetch includeLibraries(boolean includeLibraries) {
        this.includeLibraries = includeLibraries;
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
        var resolution = resolutionSupplier.get();

        var selectedDependencies = new ArrayList<Dependency>();
        if (resolution != null) {
            selectedDependencies.addAll(resolution.selectedDependencies());
        }
        selectedDependencies.addAll(this.dependencies);

        var futureFetchedLibraries = new HashMap<Library, Future<FetchedLibrary>>();
        if (this.includeLibraries) {
            selectedDependencies.forEach(dependency -> {
                futureFetchedLibraries.put(
                        dependency.library(),
                        this.executorService.submit(() ->
                                new FetchedLibrary(
                                        dependency.coordinate().getLibraryLocation(this.cache),
                                        dependency.usages()
                                )
                        )
                );
            });
        }


        var futureSources = new HashMap<Library, Future<Optional<Path>>>();
        if (this.includeSources) {
            selectedDependencies.forEach(dependency -> {
                futureSources.put(
                        dependency.library(),
                        this.executorService.submit(() ->
                                dependency.coordinate().getLibrarySourcesLocation(this.cache)
                        )
                );
            });
        }

        var futureDocumentation = new HashMap<Library, Future<Optional<Path>>>();
        if (this.includeDocumentation) {
            selectedDependencies.forEach(dependency -> {
                futureDocumentation.put(
                        dependency.library(),
                        this.executorService.submit(() ->
                                dependency.coordinate().getLibraryDocumentationLocation(this.cache)
                        )
                );
            });
        }


        var libraries = new HashMap<Library, FetchedLibrary>();
        futureFetchedLibraries.forEach((k, v) -> {
            try {
                libraries.put(k, v.get());
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });

        var sources = new HashMap<Library, Path>();
        futureSources.forEach((k, v) -> {
            try {
                v.get().ifPresent(path -> sources.put(k, path));
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });


        var documentation = new HashMap<Library, Path>();
        futureDocumentation.forEach((k, v) -> {
            try {
                v.get().ifPresent(path -> documentation.put(k, path));
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });

        return new Result(
                Map.copyOf(libraries),
                Map.copyOf(sources),
                Map.copyOf(documentation)
        );
    }

    public record Result(
            Map<Library, FetchedLibrary> fetchedLibraries,
            Map<Library, Path> sources,
            Map<Library, Path> documentation
    ) {
        public Result {
            Objects.requireNonNull(fetchedLibraries);
            Objects.requireNonNull(sources);
            Objects.requireNonNull(documentation);
        }

        public Map<Library, Path> libraries() {
            return fetchedLibraries.entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue().path()
                    ));
        }

        public String path(Usage usage, List<Path> extraPaths) {
            return Stream.concat(
                    fetchedLibraries.values().stream()
                            .filter(fetchedLibrary -> fetchedLibrary.usages().contains(usage))
                            .map(FetchedLibrary::path)
                            .map(Path::toString),
                    extraPaths.stream().map(Path::toString)
            ).collect(Collectors.joining(File.pathSeparator));
        }

        public String path(List<Path> extraPaths) {
            return Stream.concat(
                    libraries().values().stream().map(Path::toString),
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
                                    libraries().entrySet()
                                            .stream()
                                            .filter(entry -> !shouldGoOnClassPath.test(entry.getKey()))
                                            .map(Map.Entry::getValue)
                                            .map(Path::toString),
                                    extraModulePaths.stream().map(Path::toString)
                            )
                            .collect(Collectors.joining(File.pathSeparator)),

                    Stream.concat(
                                libraries().entrySet()
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
            return ModuleFinder.of(libraries().values().toArray(Path[]::new));
        }
    }
}
