package dev.mccue.resolve;

import dev.mccue.resolve.doc.Rife;

import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Contains the information required to describe a dependency version number.
 * <p>
 * This operates according to the versioning scheme specified by Maven.
 * <p>
 *
 * @param major the major version component
 * @param minor the minor version component
 * @param revision the revision of the version
 * @param qualifier a string qualifier for the version
 * @param separator the separator used to separate the qualifier from the version number
 * @author Geert Bevin (gbevin[remove] at uwyn dot com)
 * @since 1.5
 */
@Rife("https://github.com/rife2/rife2/blob/80f0ba7/lib/src/main/java/rife/bld/dependencies/VersionNumber.java")
public record VersionNumber(Integer major, Integer minor, Integer revision, String qualifier, String separator) implements Comparable<VersionNumber> {
    private static final Pattern VERSION_PATTERN = Pattern.compile("^(?<major>\\d+)(?:\\.(?<minor>\\d+)(?:\\.(?<revision>\\d+))?)?+(?:(?<separator>[.\\-])(?<qualifier>.*[^.\\-]))??$");

    public static Optional<VersionNumber> parse(String version) {
        if (version == null || version.isEmpty()) {
            return Optional.empty();
        }

        var matcher = VERSION_PATTERN.matcher(version);
        if (!matcher.matches()) {
            return Optional.empty();
        }

        var major = matcher.group("major");
        var minor = matcher.group("minor");
        var revision = matcher.group("revision");

        var major_integer = (major != null ? Integer.parseInt(major) : null);
        var minor_integer = (minor != null ? Integer.parseInt(minor) : null);
        var revision_integer = (revision != null ? Integer.parseInt(revision) : null);

        var qualifier = matcher.group("qualifier");
        var separator = matcher.group("separator");

        return Optional.of(
                new VersionNumber(major_integer, minor_integer, revision_integer, qualifier, separator)
        );
    }

    /**
     * Constructs a version number with only a major component.
     *
     * @param major the major version component
     * @since 1.5
     */
    public VersionNumber(Integer major) {
        this(major, null, null, "");
    }

    /**
     * Constructs a version number with a major and minor component.
     *
     * @param major the major version component
     * @param minor the minor version component
     * @since 1.5
     */
    public VersionNumber(Integer major, Integer minor) {
        this(major, minor, null, "");
    }

    /**
     * Constructs a version number with major, minor and revision components.
     *
     * @param major the major version component
     * @param minor the minor version component
     * @param revision the version revision component
     * @since 1.5
     */
    public VersionNumber(Integer major, Integer minor, Integer revision) {
        this(major, minor, revision, "");
    }

    /**
     * Constructs a complete version number with qualifier, the separator will default to "{@code -}".
     *
     * @param major the major version component
     * @param minor the minor version component
     * @param revision the version revision component
     * @param qualifier the version qualifier
     * @since 1.5
     */
    public VersionNumber(Integer major, Integer minor, Integer revision, String qualifier) {
        this(major, minor, revision, qualifier, "-");
    }

    /**
     * Constructs a complete version number with qualifier.
     *
     * @param major the major version component
     * @param minor the minor version component
     * @param revision the version revision component
     * @param qualifier the version qualifier
     * @param separator the separator for the version qualifier
     * @since 1.5
     */
    public VersionNumber(Integer major, Integer minor, Integer revision, String qualifier, String separator) {
        this.major = major;
        this.minor = minor;
        this.revision = revision;
        this.qualifier = (qualifier == null ? "" : qualifier);
        this.separator = separator;
    }

    /**
     * Retrieves the base version number without the qualifier.
     *
     * @return the base version number instance
     * @since 1.5
     */
    public VersionNumber getBaseVersion() {
        return new VersionNumber(major, minor, revision, null);
    }

    /**
     * Returns a primitive integer for the major version component.
     *
     * @return the major version component as an {@code int}
     * @since 1.5
     */
    public int majorInt() {
        return major == null ? 0 : major;
    }

    /**
     * Returns a primitive integer for the minor version component.
     *
     * @return the minor version component as an {@code int}
     * @since 1.5
     */
    public int minorInt() {
        return minor == null ? 0 : minor;
    }

    /**
     * Returns a primitive integer for the version revision component.
     *
     * @return the version revision component as an {@code int}
     * @since 1.5
     */
    public int revisionInt() {
        return revision == null ? 0 : revision;
    }

    public int compareTo(VersionNumber other) {
        return Comparator.comparing(VersionNumber::majorInt)
                .thenComparing(VersionNumber::minorInt)
                .thenComparing(VersionNumber::revisionInt)
                .thenComparing(VersionNumber::qualifier)
                .compare(this, other);
    }

    public String toString() {
        var version = new StringBuilder();
        version.append(majorInt());
        if (minor != null || revision != null) {
            version.append(".");
            version.append(minorInt());
        }
        if (revision != null) {
            version.append(".");
            version.append(revisionInt());
        }
        if (qualifier != null && !qualifier.isEmpty()) {
            version.append(separator);
            version.append(qualifier);
        }
        return version.toString();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof VersionNumber versionNumber
                && compareTo(versionNumber) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(majorInt(), minorInt(), revisionInt(), qualifier);
    }
}
