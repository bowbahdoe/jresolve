package dev.mccue.resolve;

import dev.mccue.resolve.doc.ToolsDeps;

import java.nio.file.Path;

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
    Coordinate NONE = new Coordinate() {
        private static final CoordinateId NONE_ID = new CoordinateId() {
            @Override
            public int hashCode() {
                return 0;
            }

            @Override
            public boolean equals(Object obj) {
                return obj == this;
            }

            @Override
            public String toString() {
                return "NONE_ID";
            }
        };

        @Override
        public VersionComparison compareVersions(Coordinate coordinate) {
            return coordinate == this ? VersionComparison.EQUAL_TO : VersionComparison.INCOMPARABLE;
        }

        @Override
        public CoordinateId id() {
            return NONE_ID;
        }

        @Override
        public Manifest getManifest(Library library, Cache cache) {
            return Manifest.EMPTY;
        }

        @Override
        public Path getLibraryLocation(Library library, Cache cache) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return "NONE";
        }
    };

    /**
     * Result of a comparison between two coordinates.
     */
    enum VersionComparison {
        /**
         * The two coordinate types cannot be compared to one another.
         */
        INCOMPARABLE,
        GREATER_THAN,
        LESS_THAN,
        EQUAL_TO;


        public static VersionComparison fromInt(int comparisonResult) {
            return comparisonResult == 0 ? EQUAL_TO : comparisonResult > 0 ? GREATER_THAN : LESS_THAN;
        }
    }

    VersionComparison compareVersions(Coordinate coordinate);

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
}
