package dev.mccue.resolve;

import java.util.ArrayList;
import java.util.List;

final class Trace extends ArrayList<Trace.Entry> {

    record Entry(
            List<DependencyId> path,
            Library library,
            CoordinateId coordinateId,
            InclusionDecision inclusionDecision
    ) {}
}
