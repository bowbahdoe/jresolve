package dev.mccue.resolve.core;

import dev.mccue.resolve.doc.Coursier;
import dev.mccue.resolve.util.Tuple2;
import dev.mccue.resolve.util.Tuple4;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Coursier("https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/core/MinimizedExclusions.scala")
public final class MinimizedExclusions {
    static final MinimizedExclusions ZERO = new MinimizedExclusions(ExcludeNone.INSTANCE);
    static final MinimizedExclusions ONE = new MinimizedExclusions(ExcludeAll.INSTANCE);

    private int hash;

    private final ExclusionData exclusionData;

    public MinimizedExclusions(ExclusionData exclusionData) {
        this.hash = 0;
        this.exclusionData = Objects.requireNonNull(
                exclusionData,
                "exclusionData must not be null"
        );
    }

    public static MinimizedExclusions of(
            Set<Tuple2<Organization, ModuleName>> exclusions
    ) {
        return of(List.copyOf(exclusions));
    }

    public static MinimizedExclusions of(
            List<Tuple2<Organization, ModuleName>> exclusions
    ) {
        if (exclusions.isEmpty()) {
            return ZERO;
        }

        var excludeByOrg0 = new HashSet<Organization>();
        var excludeByName0 = new HashSet<ModuleName>();
        var remaining0 = new HashSet<Tuple2<Organization, ModuleName>>();

        for (var exclusion : exclusions) {
            if (Organization.ALL.equals(exclusion.first())) {
                if (ModuleName.ALL.equals(exclusion.second())) {
                    return ONE;
                } else {
                    excludeByName0.add(exclusion.second());
                }
            } else if (ModuleName.ALL.equals(exclusion.second())) {
                excludeByOrg0.add(exclusion.first());
            } else {
                remaining0.add(exclusion);
            }
        }

        return new MinimizedExclusions(new ExcludeSpecific(
                Set.copyOf(excludeByOrg0),
                Set.copyOf(excludeByName0),
                Set.copyOf(remaining0)
        ));
    }

    public boolean shouldInclude(Organization organization, ModuleName moduleName) {
        return this.exclusionData.shouldInclude(organization, moduleName);
    }

    public MinimizedExclusions join(MinimizedExclusions other) {
        var newData = this.exclusionData.join(other.exclusionData);
        if (newData == this.exclusionData) {
            return this;
        }
        else if (newData == other.exclusionData) {
            return other;
        }
        else {
            return new MinimizedExclusions(newData);
        }
    }

    public MinimizedExclusions meet(MinimizedExclusions other) {
        var newData = this.exclusionData.meet(other.exclusionData);
        if (newData == this.exclusionData) {
            return this;
        }
        else if (newData == other.exclusionData) {
            return other;
        }
        else {
            return new MinimizedExclusions(newData);
        }
    }

    public MinimizedExclusions map(Function<String, String> f) {
        var newData = this.exclusionData.map(f);
        if (newData == this.exclusionData) {
            return this;
        }
        else {
            return new MinimizedExclusions(newData);
        }
    }

    public Tuple4<Boolean,
            Set<Organization>,
            Set<ModuleName>,
            Set<Tuple2<Organization, ModuleName>>
            > partitioned() {
        return this.exclusionData.partitioned();
    }


    public int size() {
        return this.exclusionData.size();
    }

    public boolean subsetOf(MinimizedExclusions other) {
        return this.exclusionData.subsetOf(other.exclusionData);
    }

    public Set<Tuple2<Organization, ModuleName>> toSet() {
        return this.exclusionData.toSet();
    }

    public sealed interface ExclusionData {
        boolean shouldInclude(
                Organization organization,
                ModuleName module
        );

        ExclusionData join(ExclusionData other);
        ExclusionData meet(ExclusionData other);

        Tuple4<Boolean,
                Set<Organization>,
                Set<ModuleName>,
                Set<Tuple2<Organization, ModuleName>>
                > partitioned();

        ExclusionData map(Function<String, String> f);

        int size();

        boolean subsetOf(ExclusionData other);

        Set<Tuple2<Organization, ModuleName>> toSet();
    }

    public enum ExcludeNone implements ExclusionData {
        INSTANCE;

        @Override
        public boolean shouldInclude(Organization organization, ModuleName module) {
            return true;
        }

        @Override
        public ExclusionData join(ExclusionData other) {
            return other;
        }

        @Override
        public ExclusionData meet(ExclusionData other) {
            return ExcludeNone.INSTANCE;
        }

        @Override
        public Tuple4<Boolean, Set<Organization>, Set<ModuleName>, Set<Tuple2<Organization, ModuleName>>> partitioned() {
            return new Tuple4<>(
                    false,
                    Set.of(),
                    Set.of(),
                    Set.of()
            );
        }

        @Override
        public ExclusionData map(Function<String, String> f) {
            return this;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean subsetOf(ExclusionData other) {
            return true;
        }

        @Override
        public Set<Tuple2<Organization, ModuleName>> toSet() {
            return Set.of();
        }


        @Override
        public String toString() {
            return "ExcludeNone";
        }
    }

    public enum ExcludeAll implements ExclusionData {
        INSTANCE;

        @Override
        public boolean shouldInclude(Organization organization, ModuleName module) {
            return false;
        }

        @Override
        public ExclusionData join(ExclusionData other) {
            return ExcludeAll.INSTANCE;
        }

        @Override
        public ExclusionData meet(ExclusionData other) {
            return other;
        }

        @Override
        public Tuple4<Boolean, Set<Organization>, Set<ModuleName>, Set<Tuple2<Organization, ModuleName>>> partitioned() {
            return new Tuple4<>(
                    true,
                    Set.of(),
                    Set.of(),
                    Set.of()
            );
        }

        @Override
        public ExclusionData map(Function<String, String> f) {
            return this;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public boolean subsetOf(ExclusionData other) {
            return this.equals(other);
        }

        @Override
        public Set<Tuple2<Organization, ModuleName>> toSet() {
            return Set.of(new Tuple2<>(Organization.ALL, ModuleName.ALL));
        }

        @Override
        public String toString() {
            return "ExcludeAll";
        }
    }

    public record ExcludeSpecific(
            Set<Organization> byOrg,
            Set<ModuleName> byModule,
            Set<Tuple2<Organization, ModuleName>> specific
    ) implements ExclusionData {
        public ExcludeSpecific(
                Set<Organization> byOrg,
                Set<ModuleName> byModule,
                Set<Tuple2<Organization, ModuleName>> specific
        ) {
            this.byOrg = Set.copyOf(byOrg);
            this.byModule = Set.copyOf(byModule);
            this.specific = Set.copyOf(specific);
        }

        @Override
        public boolean shouldInclude(Organization organization, ModuleName module) {
            return !this.byOrg.contains(organization)
                    && !this.byModule.contains(module)
                    && !this.specific.contains(new Tuple2<>(organization, module));
        }

        @Override
        public ExclusionData join(ExclusionData other) {
            return null;
        }

        @Override
        public ExclusionData meet(ExclusionData other) {
            return null;
        }

        @Override
        public Tuple4<Boolean, Set<Organization>, Set<ModuleName>, Set<Tuple2<Organization, ModuleName>>> partitioned() {
            return new Tuple4<>(
                    false,
                    byOrg,
                    byModule,
                    specific
            );
        }

        @Override
        public ExclusionData map(Function<String, String> f) {
            return new ExcludeSpecific(
                    byOrg.stream()
                            .map(org -> org.map(f))
                            .collect(Collectors.toUnmodifiableSet()),
                    byModule.stream()
                            .map(module -> module.map(f))
                            .collect(Collectors.toUnmodifiableSet()),
                    specific.stream()
                            .map(orgAndModule -> new Tuple2<>(
                                    orgAndModule.first().map(f),
                                    orgAndModule.second().map(f)
                            ))
                            .collect(Collectors.toUnmodifiableSet())
            );
        }

        @Override
        public int size() {
            return byOrg.size() + byModule().size() + specific.size();
        }

        @Override
        public boolean subsetOf(ExclusionData other) {
            return switch (other) {
                case ExcludeNone __ -> false;
                case ExcludeAll __ -> false; // This seems wrong
                case ExcludeSpecific excludeSpecific ->
                        excludeSpecific.byOrg.containsAll(byOrg)
                        && excludeSpecific.byModule.containsAll(byModule)
                        && excludeSpecific.specific.containsAll(specific);
            };
        }

        @Override
        public Set<Tuple2<Organization, ModuleName>> toSet() {
            var set = new HashSet<Tuple2<Organization, ModuleName>>();
            byOrg.stream()
                    .map(org -> new Tuple2<>(org, ModuleName.ALL))
                    .forEach(set::add);
            byModule.stream()
                    .map(module -> new Tuple2<>(Organization.ALL, module))
                    .forEach(set::add);
            set.addAll(specific);

            return Set.copyOf(set);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof MinimizedExclusions minimizedExclusions &&
                this.exclusionData.equals(minimizedExclusions.exclusionData);
    }

    @Override
    public int hashCode() {
        int cached = this.hash;
        if (cached == 0) {
            cached = this.exclusionData.hashCode();
            this.hash = cached;
            return cached;
        }
        else {
            return this.hash;
        }
    }
}
