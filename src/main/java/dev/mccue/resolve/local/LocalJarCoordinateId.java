package dev.mccue.resolve.local;

import dev.mccue.resolve.CoordinateId;

import java.nio.file.Path;

record LocalJarCoordinateId(Path path) implements CoordinateId {
}
