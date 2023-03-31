package dev.mccue.resolve;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ExecutorService;

public final class Fetch {
    private final Resolve resolve;
    private final LinkedHashSet<Classifier> classifiers;

    public Fetch() {
        this.resolve = new Resolve();
        this.classifiers = new LinkedHashSet<>();
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


    public Fetch withExecutorService(ExecutorService executorService) {
        this.resolve.withExecutorService(executorService);
        return this;
    }

    public Fetch addClassifier(Classifier classifier) {
        this.classifiers.add(classifier);
        return this;
    }

    public Fetch addClassifiers(List<Classifier> classifiers) {
        classifiers.forEach(this::addClassifier);
        return this;
    }
}
