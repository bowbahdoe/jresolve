package dev.mccue.resolve;

import java.util.ArrayList;
import java.util.List;

public final class Trace extends ArrayList<Trace.Entry> {

    record Entry(
            List<DependencyId> path,
            Library library,
            Coordinate coordinate,
            Coordinate originalCoordinate,
            CoordinateId coordinateId,
            InclusionDecision inclusionDecision
    ) {}
}
