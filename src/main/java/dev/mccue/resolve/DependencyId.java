package dev.mccue.resolve;


public record DependencyId(
        Library library,
        CoordinateId coordinateId
) {
    public DependencyId(Dependency dependency) {
        this(dependency.library(), dependency.coordinate().id());
    }
}
