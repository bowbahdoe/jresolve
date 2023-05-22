package dev.mccue.resolve;

import java.util.ArrayList;
import java.util.List;

public final class Trace extends ArrayList<Trace.Entry> {

    sealed interface Entry {}

    record StartResolution() implements Entry {}
    record FinishResolution() implements Entry {}

    record Entry_(
            List<DependencyId> path,
            Library library,
            Coordinate coordinate,
            Coordinate originalCoordinate,
            CoordinateId coordinateId,
            InclusionDecision inclusionDecision
    ) {}
}
