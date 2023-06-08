package dev.mccue.resolve;

import dev.mccue.resolve.Version;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

@NullMarked
public record VersionRange(
        Bound start,
        Bound end
) {
    public VersionRange {
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);
    }

    public VersionRange(
            @Nullable Version start,
            @Nullable Version end,
            boolean startInclusive,
            boolean endInclusive
    ) {
        this(
                start == null
                        ? new Bound.Unbounded()
                        : startInclusive
                        ? new Bound.Inclusive(start)
                        : new Bound.Exclusive(start),
                end == null
                        ? new Bound.Unbounded()
                        : endInclusive
                        ? new Bound.Inclusive(end)
                        : new Bound.Exclusive(end)
        );
    }

    sealed interface Bound {
        record Inclusive(Version version) implements Bound {}
        record Exclusive(Version version) implements Bound {}
        record Unbounded() implements Bound {}
    }

    public boolean includes(Version version) {
        return switch (start) {
            case Bound.Unbounded __ -> true;
            case Bound.Inclusive(Version inclusiveStart) -> version.compareTo(inclusiveStart) >= 0;
            case Bound.Exclusive(Version exclusiveStart) -> version.compareTo(exclusiveStart) > 0;
        } && switch (end) {
            case Bound.Unbounded __ -> true;
            case Bound.Inclusive(Version inclusiveEnd) -> version.compareTo(inclusiveEnd) <= 0;
            case Bound.Exclusive(Version exclusiveEnd) -> version.compareTo(exclusiveEnd) < 0;
        };
    }
}
