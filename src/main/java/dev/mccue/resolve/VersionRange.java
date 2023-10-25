package dev.mccue.resolve;

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
        boolean startOk = switch (start) {
            case Bound.Inclusive inclusive ->
                version.compareTo(inclusive.version) >= 0;
            case Bound.Exclusive exclusive ->
                version.compareTo(exclusive.version) > 0;
            case Bound.Unbounded() -> true;
        };

        boolean endOk = switch (end) {
            case Bound.Inclusive inclusive ->
                    version.compareTo(inclusive.version) <= 0;
            case Bound.Exclusive exclusive ->
                    version.compareTo(exclusive.version) < 0;
            case Bound.Unbounded() -> true;
        };

        return startOk && endOk;
    }
}
