package dev.mccue.resolve.maven;

import java.util.regex.Pattern;

public final class MavenRepository {
    private static final Pattern SNAPSHOT_TIMESTAMP =
            Pattern.compile("(.*-)?[0-9]{8}\\.[0-9]{6}-[0-9]+");

    public static boolean isSnapshot(String version) {
        return version.endsWith("SNAPSHOT")
                || SNAPSHOT_TIMESTAMP.matcher(version).matches();
    }


}
