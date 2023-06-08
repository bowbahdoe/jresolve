package dev.mccue.resolve;

import java.util.List;

public record CacheKey(List<String> components) {
    public CacheKey(List<String> components) {
        this.components = List.copyOf(components);
    }
}
