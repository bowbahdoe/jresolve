package dev.mccue.resolve.tdeps;

import dev.mccue.resolve.core.MinimizedExclusions;
import dev.mccue.resolve.util.LL;

import java.util.Map;
import java.util.Optional;

public final class ExclusionsMap {
    private final LL<Map.Entry<Lib, MinimizedExclusions>> exclusions;

    public ExclusionsMap() {
        this(new LL.Nil<>());
    }

    private ExclusionsMap(LL<Map.Entry<Lib, MinimizedExclusions>> exclusions) {
        this.exclusions = exclusions;
    }

    public ExclusionsMap with(Lib key, MinimizedExclusions value) {
        return new ExclusionsMap(
                new LL.Cons<>(Map.entry(key, value), this.exclusions)
        );
    }

    public Optional<MinimizedExclusions> get(Lib key) {
        var exclusions = this.exclusions;
        while (exclusions instanceof LL.Cons<Map.Entry<Lib, MinimizedExclusions>> cons) {
            var entry = cons.head();
            if (entry.getKey().equals(key)) {
                return Optional.of(entry.getValue());
            }
            exclusions = cons.tail();
        }

        return Optional.empty();
    }
}
