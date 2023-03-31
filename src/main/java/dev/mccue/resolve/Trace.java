package dev.mccue.resolve;

import java.util.ArrayList;

public final class Trace extends ArrayList<Trace.Entry> {
    record Entry(
            Object path,
            Library library,
            Coordinate coordinate,
            Coordinate originalCoordinate,
            CoordinateId coordinateId,
            InclusionDecision inclusionDecision
    ) {}
}
