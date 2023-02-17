package dev.mccue.resolve.tdeps;

import java.util.Map;

public record ExpansionInput(
        Map<Lib, Coordinate> initialDependencies,
        Map<Lib, Coordinate> defaultDeps,
        Map<Lib, Coordinate> overrideDeps
) {
}
