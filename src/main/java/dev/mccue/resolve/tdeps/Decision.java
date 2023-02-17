package dev.mccue.resolve.tdeps;

import java.util.List;

public record Decision(
        Lib lib,
        Coordinate coord,
        CoordinateId coordId,
        List<Path> paths,
        boolean include,
        Reason reason
) {
}
