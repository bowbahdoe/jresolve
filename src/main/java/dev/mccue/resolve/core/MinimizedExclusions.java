package dev.mccue.resolve.core;

import dev.mccue.resolve.doc.Coursier;
import dev.mccue.resolve.util.Tuple4;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Coursier("https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/core/MinimizedExclusions.scala")
public final class MinimizedExclusions {
    static final MinimizedExclusions NONE = new MinimizedExclusions(ExcludeNone.INSTANCE);
    static final MinimizedExclusions ALL = new MinimizedExclusions(ExcludeAll.INSTANCE);

    private int hash;

    private final ExclusionData exclusionData;

    private MinimizedExclusions(ExclusionData exclusionData) {
        this.hash = 0;
        this.exclusionData = Objects.requireNonNull(
                exclusionData,
                "exclusionData must not be null"
        );
    }

    public static MinimizedExclusions of(
            Set<Exclusion> exclusions
    ) {
        return of(List.copyOf(exclusions));
    }

    public static MinimizedExclusions of(
            List<Exclusion> exclusions
    ) {
        if (exclusions.isEmpty()) {
            return NONE;
        }

        var excludeByOrg0 = new HashSet<Organization>();
        var excludeByName0 = new HashSet<ModuleName>();
        var remaining0 = new HashSet<Exclusion>();

        for (var exclusion : exclusions) {
            if (Organization.ALL.equals(exclusion.organization())) {
                if (ModuleName.ALL.equals(exclusion.moduleName())) {
                    return ALL;
                } else {
                    excludeByName0.add(exclusion.moduleName());
                }
            } else if (ModuleName.ALL.equals(exclusion.moduleName())) {
                excludeByOrg0.add(exclusion.organization());
            } else {
                remaining0.add(exclusion);
            }
        }

        return new MinimizedExclusions(new ExcludeSpecific(
                Set.copyOf(excludeByOrg0),
                Set.copyOf(excludeByName0),
                remaining0.stream()
                        .filter(exclusion ->
                                !excludeByOrg0.contains(exclusion.organization())
                                    && !excludeByName0.contains(exclusion.moduleName()))
                        .collect(Collectors.toUnmodifiableSet()))
        );
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
            Set<Exclusion>
            > partitioned() {
        return this.exclusionData.partitioned();
    }


    public int size() {
        return this.exclusionData.size();
    }

    public boolean subsetOf(MinimizedExclusions other) {
        return this.exclusionData.subsetOf(other.exclusionData);
    }

    public Set<Exclusion> toSet() {
        return this.exclusionData.toSet();
    }

    public sealed interface ExclusionData {
        boolean shouldInclude(
                Organization organization,
                ModuleName moduleName
        );

        ExclusionData join(ExclusionData other);
        ExclusionData meet(ExclusionData other);

        Tuple4<
                Boolean,
                Set<Organization>,
                Set<ModuleName>,
                Set<Exclusion>
                > partitioned();

        ExclusionData map(Function<String, String> f);

        int size();

        boolean subsetOf(ExclusionData other);

        Set<Exclusion> toSet();
    }

    public enum ExcludeNone implements ExclusionData {
        INSTANCE;

        @Override
        public boolean shouldInclude(Organization organization, ModuleName moduleName) {
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
        public Tuple4<Boolean, Set<Organization>, Set<ModuleName>, Set<Exclusion>> partitioned() {
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
        public Set<Exclusion> toSet() {
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
        public boolean shouldInclude(Organization organization, ModuleName moduleName) {
            return false;
        }

        @Override
        public ExclusionData join(ExclusionData other) {
            return this;
        }

        @Override
        public ExclusionData meet(ExclusionData other) {
            return other;
        }

        @Override
        public Tuple4<Boolean, Set<Organization>, Set<ModuleName>, Set<Exclusion>> partitioned() {
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
        public Set<Exclusion> toSet() {
            return Set.of(new Exclusion(Organization.ALL, ModuleName.ALL));
        }

        @Override
        public String toString() {
            return "ExcludeAll";
        }
    }

    public record ExcludeSpecific(
            Set<Organization> byOrg,
            Set<ModuleName> byModuleName,
            Set<Exclusion> specific
    ) implements ExclusionData {
        public ExcludeSpecific(
                Set<Organization> byOrg,
                Set<ModuleName> byModuleName,
                Set<Exclusion> specific
        ) {
            this.byOrg = Set.copyOf(byOrg);
            this.byModuleName = Set.copyOf(byModuleName);
            this.specific = Set.copyOf(specific);
        }

        @Override
        public boolean shouldInclude(Organization organization, ModuleName moduleName) {
            return !this.byOrg.contains(organization)
                    && !this.byModuleName.contains(moduleName)
                    && !this.specific.contains(new Exclusion(organization, moduleName));
        }

        @Override
        public ExclusionData join(ExclusionData other) {
            return switch (other) {
                case ExcludeNone __ -> this;
                case ExcludeAll all -> all;
                case ExcludeSpecific(
                        Set<Organization> otherByOrg,
                        Set<ModuleName> otherByModuleName,
                        Set<Exclusion> otherSpecific
                ) -> {

                    var joinedByOrg = new HashSet<Organization>();
                    joinedByOrg.addAll(this.byOrg);
                    joinedByOrg.addAll(otherByOrg);

                    var joinedByModule = new HashSet<ModuleName>();
                    joinedByModule.addAll(this.byModuleName);
                    joinedByModule.addAll(otherByModuleName);

                    var joinedSpecific = new HashSet<Exclusion>();
                    this.specific
                            .stream()
                            .filter(exclusion ->
                                    !otherByOrg.contains(exclusion.organization()) &&
                                        !otherByModuleName.contains(exclusion.moduleName()))
                            .forEach(joinedSpecific::add);

                    otherSpecific
                            .stream()
                            .filter(exclusion ->
                                    !byOrg.contains(exclusion.organization()) &&
                                            !byModuleName.contains(exclusion.moduleName()))
                            .forEach(joinedSpecific::add);

                    yield new ExcludeSpecific(
                            Set.copyOf(joinedByOrg),
                            Set.copyOf(joinedByModule),
                            Set.copyOf(joinedSpecific)
                    );
                }
            };
        }

        @Override
        public ExclusionData meet(ExclusionData other) {
            return switch (other) {
                case ExcludeNone none -> none;
                case ExcludeAll __ -> this;
                case ExcludeSpecific(
                        Set<Organization> otherByOrg,
                        Set<ModuleName> otherByModule,
                        Set<Exclusion> otherSpecific
                )  -> {
                    var metByOrg = byOrg.stream()
                            .filter(otherByOrg::contains)
                            .collect(Collectors.toUnmodifiableSet());

                    var metByModule = byModuleName.stream()
                            .filter(otherByModule::contains)
                            .collect(Collectors.toUnmodifiableSet());

                    var metSpecific = new HashSet<Exclusion>();
                    specific.stream()
                            .filter(exclusion -> {
                                var org = exclusion.organization();
                                var moduleName = exclusion.moduleName();
                                return otherByOrg.contains(org) ||
                                        otherByModule.contains(moduleName) ||
                                        otherSpecific.contains(exclusion);
                            })
                            .forEach(metSpecific::add);

                    otherSpecific.stream()
                            .filter(exclusion -> {
                                var org = exclusion.organization();
                                var moduleName = exclusion.moduleName();
                                return byOrg.contains(org) ||
                                        byModuleName.contains(moduleName) ||
                                        specific.contains(exclusion);
                            })
                            .forEach(metSpecific::add);

                    if (metByOrg.isEmpty() && metByModule.isEmpty() && metSpecific.isEmpty()) {
                        yield ExcludeNone.INSTANCE;
                    }
                    else {
                        yield new ExcludeSpecific(
                                metByOrg,
                                metByModule,
                                Set.copyOf(metSpecific)
                        );
                    }


                }
            };
        }

        @Override
        public Tuple4<Boolean, Set<Organization>, Set<ModuleName>, Set<Exclusion>> partitioned() {
            return new Tuple4<>(
                    false,
                    byOrg,
                    byModuleName,
                    specific
            );
        }

        @Override
        public ExclusionData map(Function<String, String> f) {
            return new ExcludeSpecific(
                    byOrg.stream()
                            .map(org -> org.map(f))
                            .collect(Collectors.toUnmodifiableSet()),
                    byModuleName.stream()
                            .map(moduleName -> moduleName.map(f))
                            .collect(Collectors.toUnmodifiableSet()),
                    specific.stream()
                            .map(exclusion -> new Exclusion(
                                    exclusion.organization().map(f),
                                    exclusion.moduleName().map(f)
                            ))
                            .collect(Collectors.toUnmodifiableSet())
            );
        }

        @Override
        public int size() {
            return byOrg.size() + byModuleName().size() + specific.size();
        }

        @Override
        public boolean subsetOf(ExclusionData other) {
            return switch (other) {
                case ExcludeNone __ -> false;
                case ExcludeAll __ -> false; // This seems wrong
                case ExcludeSpecific excludeSpecific ->
                        excludeSpecific.byOrg.containsAll(byOrg)
                        && excludeSpecific.byModuleName.containsAll(byModuleName)
                        && excludeSpecific.specific.containsAll(specific);
            };
        }

        @Override
        public Set<Exclusion> toSet() {
            var set = new HashSet<Exclusion>();
            byOrg.stream()
                    .map(org -> new Exclusion(org, ModuleName.ALL))
                    .forEach(set::add);
            byModuleName.stream()
                    .map(moduleName -> new Exclusion(Organization.ALL, moduleName))
                    .forEach(set::add);
            set.addAll(specific);

            return Set.copyOf(set);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return (this == obj) || (
                obj instanceof MinimizedExclusions minimizedExclusions &&
                this.exclusionData.equals(minimizedExclusions.exclusionData)
        );
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

    @Override
    public String toString() {
        return "MinimizedExclusions[" +
                "exclusionData=" + exclusionData +
                ']';
    }
}
