package dev.mccue.resolve.maven;

import dev.mccue.resolve.*;

import java.util.*;
import java.util.regex.Pattern;

public record PomDependency(
        Library library,
        String version,
        Set<Exclusion> exclusions,
        Type type,
        Classifier classifier,
        boolean optional
) {

    public PomDependency(
            Library library,
            String version
    ) {
        this(library, version, Set.of(), Type.EMPTY, Classifier.EMPTY, false);
    }

    private static final Pattern MAVEN_PROPERTY = Pattern.compile("\\$\\{([^<>{}]+)}");

    private String resolveProperties(Map<String, String> properties, String data) {
        if (data == null) {
            return null;
        }

        var processed_data = new StringBuilder();
        var matcher = MAVEN_PROPERTY.matcher(data);
        var last_end = 0;
        while (matcher.find()) {
            if (matcher.groupCount() == 1) {
                var property = matcher.group(1);
                if (properties.containsKey(property)) {
                    processed_data.append(data, last_end, matcher.start());
                    processed_data.append(properties.get(property));
                    last_end = matcher.end();
                }
            }
        }
        if (last_end < data.length()) {
            processed_data.append(data.substring(last_end));
        }

        return processed_data.toString();
    }
}
