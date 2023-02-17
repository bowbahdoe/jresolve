package dev.mccue.resolve.tdeps;

public enum Reason {
    NEW_TOP_DEP,
    NEW_DEP,
    SAME_VERSION,
    NEWER_VERSION,
    USE_TOP,
    OLDER_VERSION,
    EXCLUDED,
    PARENT_OMITTED,
    SUPERSEDED
}
