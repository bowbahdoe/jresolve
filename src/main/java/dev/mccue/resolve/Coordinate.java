package dev.mccue.resolve;

import dev.mccue.resolve.doc.ToolsDeps;

import java.nio.file.Path;
import java.util.Optional;

/**
 * A Coordinate says where you can find information about a dependency.
 * Namely
 *
 * <ul>
 *     <li>The artifact to put on the class/module path.</li>
 *     <li>The artifact containing library sources.</li>
 *     <li>The artifact containing library documentation.</li>
 *     <li>The manifest of required transitive dependencies.</li>
 * </ul>
 */
@ToolsDeps(
        value = "https://clojure.org/reference/dep_expansion",
        details = """
        Most resolvers are built around specifically using maven versions for
        dependencies. tools.deps is the only one where the set of potential coordinate
        types is open to extension for local/git/etc.
        
        The term 'coord' is what they use.
        """
)
public interface Coordinate {
    /**
     * In some situations a coordinate might need to consult an external source
     * to know exactly what to do to locate a library.
     *
     * <p>
     *     The big use case of this are version ranges which, while an affront to <b>god</b>
     *     are nonetheless in many published POMs.
     * </p>
     *
     * <p>
     *     Another is snapshot versioning, equally sinful.
     * </p>
     *
     * @return The normalized coordinate
     */
    default Coordinate normalize(Library library, Cache cache) {
        return this;
    }

    /**
     * Result of a comparison between two coordinates.
     */
    enum VersionOrdering {
        /**
         * The two coordinate types cannot be compared to one another.
         */
        INCOMPARABLE,
        GREATER_THAN,
        LESS_THAN,
        EQUAL_TO;


        public static VersionOrdering fromInt(int comparisonResult) {
            return comparisonResult == 0
                    ? EQUAL_TO
                    : comparisonResult > 0
                    ? GREATER_THAN
                    : LESS_THAN;
        }
    }

    VersionOrdering compareVersions(Coordinate coordinate);

    /**
     * @return An object that is safe to use as a key in hashmaps which acts as an "id"
     * for the coordinate.
     */
    CoordinateId id();

    Manifest getManifest(Library library, Cache cache);

    /**
     * Gets the location of the given library on disk, assuming the library was located
     * with this coordinate.
     *
     * <p>
     *     If the library is not downloaded on disk, this method will do so before returning.
     * </p>
     */
    Path getLibraryLocation(Library library, Cache cache);

    default Optional<Path> getLibrarySourcesLocation(Library library, Cache cache) {
        return Optional.empty();
    }

    default Optional<Path> getLibraryDocumentationLocation(Library library, Cache cache) {
        return Optional.empty();
    }
}
