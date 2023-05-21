package dev.mccue.resolve;

import java.util.LinkedHashMap;
import java.util.List;

public final class Resolve {
    private final LinkedHashMap<Library, Dependency> dependencies;
    private final LinkedHashMap<Library, Dependency> dependencyOverrides;

    private final LinkedHashMap<Library, Dependency> dependencyDefaults;
    Cache cache;

    public Resolve() {
        this.dependencies = new LinkedHashMap<>();
        this.dependencyOverrides = new LinkedHashMap<>();
        this.dependencyDefaults = new LinkedHashMap<>();
        this.cache = new StandardCache();
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

    public Resolve addDependencyOverrides(List<Dependency> dependencies) {
        dependencies.forEach(this::addDependencyOverride);
        return this;
    }

    public Resolve addDependencyDefault(Dependency dependency) {
        this.dependencyDefaults.put(dependency.library(), dependency);
        return this;
    }

    public Resolve addDependencyDefaults(List<Dependency> dependencies) {
        dependencies.forEach(this::addDependencyDefault);
        return this;
    }



    public Resolve withCache(Cache cache) {
        this.cache = cache;
        return this;
    }

    public Resolution run() {
        return Resolution.expandDependencies(
                dependencies,
                dependencyOverrides,
                cache
        );
    }
}
