package dev.mccue.resolve;

/**
 * <p>
 *     Marker interface for a type which can act as an identifier for a {@link Coordinate}.
 * </p>
 *
 * <p>
 *     The only requirement for an implementation is that it have sensible implementations
 *     of {@link Object#equals(Object)} and {@link Object#hashCode()}.
 * </p>
 *
 * <p>
 *     A way to think about this is that a {@link Coordinate} contains information about both
 *     where to look and what to look for. This should retain only information about what is
 *     being looked for. So if a {@link dev.mccue.resolve.maven.MavenCoordinate} has information
 *     about the version of a library to look for and the repository to search for that library,
 *     this should retain only the version.
 * </p>
 */
public interface CoordinateId {
    @Override
    boolean equals(Object o);

    @Override
    int hashCode();
}