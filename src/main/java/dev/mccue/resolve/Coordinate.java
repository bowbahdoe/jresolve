package dev.mccue.resolve;

import dev.mccue.resolve.doc.ToolsDeps;

import java.util.List;

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
     * Special "none" coordinate for indicating that
     * no coordinate was supplied.
     */
    Coordinate NONE = new Coordinate() {
        @Override
        public VersionComparison compareVersions(
                Coordinate coordinate
        ) {
            return VersionComparison.INCOMPARABLE;
        }

        @Override
        public CoordinateId id() {
            return null;
        }

        @Override
        public Manifest getManifest(Library library, Cache cache) {
            return new Manifest() {
                @Override
                public List<Dependency> dependencies() {
                    return List.of();
                }
            };
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
     * @return An object safe to use as a key in hashmaps which acts as an "id"
     * for the coordinate.
     */
    CoordinateId id();

    Manifest getManifest(Library library, Cache cache);
}
