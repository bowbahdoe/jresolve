package dev.mccue.resolve;

import dev.mccue.resolve.doc.Gold;

import java.util.Arrays;
import java.util.List;

@Gold
public record CacheKey(List<String> components) {
    public CacheKey(List<String> components) {
        this.components = List.copyOf(components);
    }

    public CacheKey(String... components) {
        this(Arrays.asList(components));
    }
}
