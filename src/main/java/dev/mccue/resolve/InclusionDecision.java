package dev.mccue.resolve;

public record InclusionDecision(
        boolean include,
        Reason reason
) {
}
