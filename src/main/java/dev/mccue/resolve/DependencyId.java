package dev.mccue.resolve;

record DependencyId(
        Library library,
        CoordinateId coordinateId
) {
    DependencyId(Dependency dependency) {
        this(dependency.library(), dependency.coordinate().id());
    }
}
