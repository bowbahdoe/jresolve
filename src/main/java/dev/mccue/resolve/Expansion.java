package dev.mccue.resolve;

import dev.mccue.resolve.maven.MavenCoordinate;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public final class Expansion {

    /**
     * @param initialDependencies  each dependency is defined as a lib (symbol) and coordinate (maven, git, local, etc)
     * @param defaultDependencies a map of lib to coord to use if no coordinate is supplied
     * @param overrideDependencies a map of lib to coord to use if lib is found
     */
    public VersionMap expandDependencies(
            Map<Library, Coordinate> initialDependencies,
            Map<Library, Coordinate> defaultDependencies,
            Map<Library, Coordinate> overrideDependencies,
            ExecutorService executorService
    ) {
        var versionMap = new VersionMap();
        System.out.println(versionMap);

        return versionMap;
    }


    public static void main(String[] args) {
        System.out.println(new MavenCoordinate("1.9.0"));
        new Expansion().expandDependencies(
                Map.of(
                        new Library("org.clojure", "clojure"),
                        new MavenCoordinate("1.9.0")
                ),
                Map.of(),
                Map.of(),
                Executors.newSingleThreadExecutor()
        );
    }
}
