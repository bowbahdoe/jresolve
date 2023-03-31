package dev.mccue.resolve;

import dev.mccue.resolve.maven.MavenCoordinate;
import dev.mccue.resolve.util.LL;

import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public record Resolution(
        VersionMap versionMap,
        Trace trace
) {
    private record CutKey(Library library, CoordinateId coordinateId) {}

    static Exclusions updateExclusions(
            Library library,
            InclusionDecision inclusionDecision,
            CoordinateId coordinateId,
            LL<Library> usePath,
            HashMap<CutKey, Exclusions> cut,
            Exclusions exclusions
    ) {
        if (inclusionDecision.included()) {
            cut.put(new CutKey(library, coordinateId), exclusions);
            return exclusions;
        }
        else if (inclusionDecision == InclusionDecision.SAME_VERSION) {
            var key = new CutKey(library, coordinateId);
            var cutCoord = cut.get(key);
            var newCut = cutCoord.meet(exclusions);
            cut.put(key, newCut);
            return newCut;
        }
        else {
            return exclusions;
        }
    }

    /**
     * @param initialDependencies  each dependency is defined as a lib (symbol) and coordinate (maven, git, local, etc)
     * @param overrideDependencies a map of lib to coord to use if lib is found
     */
    static Resolution expandDependencies(
            Map<Library, Dependency> initialDependencies,
            Map<Library, Dependency> overrideDependencies,
            ExecutorService executorService,
            Cache cache
    ) {


        var cut = new HashMap<CutKey, Exclusions>();
        record QueueEntry(
                Dependency dependency,
                LL<Library> path
        ) {
        }

        Queue<QueueEntry> q = new ArrayDeque<>();
        initialDependencies.forEach((library, dependency) -> q.add(
                new QueueEntry(
                        new Dependency(library, dependency.coordinate(), dependency.exclusions()),
                        new LL.Nil<>()
                )
        ));


        var versionMap = new VersionMap();

        while (!q.isEmpty()) {
            var queueEntry = q.poll();

            var library = queueEntry.dependency.library();
            var dependency = overrideDependencies.getOrDefault(
                    library,
                    queueEntry.dependency
            );
            var coordinate = dependency.coordinate();
            var coordinateId = coordinate.id();

            var decision = versionMap.includeCoordinate(
                    dependency,
                    coordinateId,
                    queueEntry.path
            );

            var exclusions = updateExclusions(
                    library,
                    decision,
                    coordinateId,
                    queueEntry.path,
                    cut,
                    dependency.exclusions()
            );

            Manifest manifest = () -> coordinate.getManifest(library, cache)
                    .dependencies()
                    .stream()
                    .filter(dep -> exclusions.shouldInclude(dep.library()))
                    .map(dep -> dep.withExclusions(dep.exclusions().join(exclusions)))
                    .toList();

            for (var manifestDep : manifest.dependencies()) {
                q.add(new QueueEntry(
                        manifestDep,
                        queueEntry.path.prepend(queueEntry.dependency.library())
                ));
            }
        }


        return new Resolution(
                versionMap,
                null
        );
    }
}
