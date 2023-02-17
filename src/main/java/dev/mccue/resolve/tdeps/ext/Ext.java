package dev.mccue.resolve.tdeps.ext;

import dev.mccue.resolve.tdeps.Coordinate;
import dev.mccue.resolve.tdeps.CoordinateId;
import dev.mccue.resolve.tdeps.Lib;

import java.nio.file.Path;
import java.util.List;

public interface Ext {
    Path libLocation(Lib lib, Coordinate coordinate);
    int compareVersions(Lib lib, Coordinate newCoordinate, Coordinate oldCoordinate);
    Object coordDeps(Lib lib, boolean useCoord, Object manifest);
    Object manifestType(Lib lib, boolean chooseCoord);
    CoordinateId depId(Lib lib, boolean useCoord);
    List<Path> coordPaths(Lib lib, Coordinate coordinate, Object manifest);
    String coordSummary(Lib lib, Coordinate coordinate);

}
