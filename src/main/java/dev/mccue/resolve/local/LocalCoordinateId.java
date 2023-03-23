package dev.mccue.resolve.local;

import dev.mccue.resolve.CoordinateId;

import java.nio.file.Path;

public record LocalCoordinateId(Path root) implements CoordinateId {
}
