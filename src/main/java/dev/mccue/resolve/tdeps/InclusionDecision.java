package dev.mccue.resolve.tdeps;

public record InclusionDecision(
        boolean include,
        Reason reason
) {
}
