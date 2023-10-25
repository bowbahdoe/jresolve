package dev.mccue.resolve;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public final class Resolve {
    private final LinkedHashMap<Library, Dependency> dependencies;
    private final LinkedHashMap<Library, Dependency> dependencyOverrides;
    ExecutorService executorService;
    Cache cache;

    public Resolve() {
        this.dependencies = new LinkedHashMap<>();
        this.dependencyOverrides = new LinkedHashMap<>();
        this.cache = Cache.standard();
        var count = new AtomicInteger();
        this.executorService = Executors.newFixedThreadPool(8, (r) -> {
            var t = new Thread(r);
            t.setName("resolve-" + count.getAndIncrement());
            t.setDaemon(true);
            return t;
        });
    }

    public Resolve addDependency(Dependency dependency) {
        this.dependencies.put(dependency.library(), dependency);
        return this;
    }

    public Resolve addDependencies(List<Dependency> dependencies) {
        dependencies.forEach(this::addDependency);
        return this;
    }

    public Resolve addDependencyOverride(Dependency dependency) {
        this.dependencyOverrides.put(dependency.library(), dependency);
        return this;
    }

    public Resolve addDependencyOverride(Library library, Dependency dependency) {
        this.dependencyOverrides.put(library, dependency);
        return this;
    }

    public Resolve addDependencyOverrides(List<Dependency> dependencies) {
        dependencies.forEach(this::addDependencyOverride);
        return this;
    }

    public Resolve withCache(Cache cache) {
        this.cache = cache;
        return this;
    }

    public Resolve withExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
        return this;
    }

    public Resolution run() {
        return Resolution.expandDependencies(
                dependencies,
                dependencyOverrides,
                cache,
                executorService
        );
    }

    public Fetch fetch() {
        return new Fetch(this);
    }
}
