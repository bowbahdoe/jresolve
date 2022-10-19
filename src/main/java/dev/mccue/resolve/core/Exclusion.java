package dev.mccue.resolve.core;

import java.util.Objects;

public record Exclusion(
        Organization organization,
        ModuleName moduleName
) {
    public static final Exclusion ALL = new Exclusion(Organization.ALL, ModuleName.ALL);

    public Exclusion {
        Objects.requireNonNull(organization, "organization must not be null");
        Objects.requireNonNull(moduleName, "moduleName must not be null");
    }
}
