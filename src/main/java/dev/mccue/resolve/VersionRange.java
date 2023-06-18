package dev.mccue.resolve;

import dev.mccue.resolve.Version;
import dev.mccue.resolve.doc.PatternMatchHere;
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

    @PatternMatchHere
    public boolean includes(Version version) {
        boolean startOk = true;
        if (start instanceof Bound.Inclusive inclusive) {
            startOk = version.compareTo(inclusive.version) >= 0;
        }
        else if (start instanceof Bound.Exclusive exclusive) {
            startOk = version.compareTo(exclusive.version) > 0;
        }

        boolean endOk = true;
        if (end instanceof Bound.Inclusive inclusive) {
            endOk = version.compareTo(inclusive.version) <= 0;
        }
        else if (end instanceof Bound.Exclusive exclusive) {
            endOk = version.compareTo(exclusive.version) < 0;
        }

        return startOk && endOk;
    }
}
