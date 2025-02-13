package dev.mccue.resolve;

import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public final class Usages {
    private final @Nullable List<Usage> value;

    private Usages(@Nullable List<Usage> value) {
        this.value = value;
    }

    public static Usages unspecified() {
        return new Usages(null);
    }

    public static Usages specified(List<Usage> value) {
        return new Usages(List.copyOf(value));
    }

    boolean contains(Usage usage) {
        return value != null && value.contains(usage);
    }

    @Override
    public String toString() {
        if (value == null) {
            return "Usages[UNSPECIFIED]";
        }
        else {
            return "Usages" + value;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Usages usages)) return false;
        return Objects.equals(value, usages.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }
}
