package dev.mccue.resolve;

import dev.mccue.resolve.doc.Gold;

@Gold
public enum InclusionDecision {
    NEW_TOP_DEP(true),
    NEW_DEP(true),
    SAME_VERSION(false),
    NEWER_VERSION(true),
    USE_TOP(false),
    OLDER_VERSION(false),
    EXCLUDED(false),
    PARENT_OMITTED(false);

    private final boolean included;

    InclusionDecision(boolean included) {
        this.included = included;
    }

    public boolean included() {
        return this.included;
    }
}
