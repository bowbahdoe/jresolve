package dev.mccue.resolve.tdeps;

import java.util.List;

public record Trace(
        List<Decision> decisions
) {
}
