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
 */
public interface CoordinateId {
    @Override
    boolean equals(Object o);

    @Override
    int hashCode();
}