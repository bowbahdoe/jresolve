package dev.mccue.resolve.core;

import dev.mccue.resolve.doc.Coursier;
import dev.mccue.resolve.doc.Incomplete;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

@Incomplete
@Coursier("https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/core/Versions.scala")
public record Versions(
        String latest,
        String release,
        List<String> available,
        LastUpdated lastUpdated
) {

    sealed interface LastUpdated {
        record DoNotKnow() implements LastUpdated {}

        record At(
                DateTime dateTime
        ) implements LastUpdated {
            public At {
                Objects.requireNonNull(dateTime, "dateTime must not be null");
            }
        }
    }

    record DateTime(
            int year,
            int month,
            int day,
            int hour,
            int minute,
            int second
    ) implements Comparable<DateTime> {
        private static final Comparator<DateTime> COMPARATOR =
                Comparator.comparing(DateTime::year)
                        .thenComparing(DateTime::month)
                        .thenComparing(DateTime::day)
                        .thenComparing(DateTime::hour)
                        .thenComparing(DateTime::minute)
                        .thenComparing(DateTime::second);

        @Override
        public int compareTo(DateTime o) {
            return COMPARATOR.compare(this, o);
        }
    }

}
