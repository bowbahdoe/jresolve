package dev.mccue.resolve;

import dev.mccue.resolve.doc.Coursier;
import dev.mccue.resolve.util.Tuple4;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Coursier("https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/core/MinimizedExclusions.scala")
public final class Exclusions {
    public static final Exclusions NONE = new Exclusions(ExcludeNone.INSTANCE);
    public static final Exclusions ALL = new Exclusions(ExcludeAll.INSTANCE);

    private int hash;

    private final ExclusionData exclusionData;

    private Exclusions(ExclusionData exclusionData) {
        this.hash = 0;
        this.exclusionData = Objects.requireNonNull(
                exclusionData,
                "exclusionData must not be null"
        );
    }

    public static Exclusions of(
            Set<Exclusion> exclusions
    ) {
        return of(List.copyOf(exclusions));
    }

    public static Exclusions of(
            List<Exclusion> exclusions
    ) {
        if (exclusions.isEmpty()) {
            return NONE;
        }

        var excludeByOrg0 = new HashSet<Group>();
        var excludeByName0 = new HashSet<Artifact>();
        var remaining0 = new HashSet<Exclusion>();

        for (var exclusion : exclusions) {
            if (Group.ALL.equals(exclusion.group())) {
                if (Artifact.ALL.equals(exclusion.artifact())) {
                    return ALL;
                } else {
                    excludeByName0.add(exclusion.artifact());
                }
            } else if (Artifact.ALL.equals(exclusion.artifact())) {
                excludeByOrg0.add(exclusion.group());
            } else {
                remaining0.add(exclusion);
            }
        }

        return new Exclusions(new ExcludeSpecific(
                Set.copyOf(excludeByOrg0),
                Set.copyOf(excludeByName0),
                remaining0.stream()
                        .filter(exclusion ->
                                !excludeByOrg0.contains(exclusion.group())
                                    && !excludeByName0.contains(exclusion.artifact()))
                        .collect(Collectors.toUnmodifiableSet()))
        );
    }

    public boolean shouldInclude(Library library) {
        return this.exclusionData.shouldInclude(library.group(), library.artifact());
    }

    public Exclusions join(Exclusions other) {
        var newData = this.exclusionData.join(other.exclusionData);
        if (newData == this.exclusionData) {
            return this;
        }
        else if (newData == other.exclusionData) {
            return other;
        }
        else {
            return new Exclusions(newData);
        }
    }

    public Exclusions meet(Exclusions other) {
        var newData = this.exclusionData.meet(other.exclusionData);
        if (newData == this.exclusionData) {
            return this;
        }
        else if (newData == other.exclusionData) {
            return other;
        }
        else {
            return new Exclusions(newData);
        }
    }

    public Exclusions map(Function<String, String> f) {
        var newData = this.exclusionData.map(f);
        if (newData == this.exclusionData) {
            return this;
        }
        else {
            return new Exclusions(newData);
        }
    }

    public Tuple4<Boolean,
            Set<Group>,
            Set<Artifact>,
            Set<Exclusion>
            > partitioned() {
        return this.exclusionData.partitioned();
    }


    public int size() {
        return this.exclusionData.size();
    }

    public boolean subsetOf(Exclusions other) {
        return this.exclusionData.subsetOf(other.exclusionData);
    }

    public Set<Exclusion> toSet() {
        return this.exclusionData.toSet();
    }

    public sealed interface ExclusionData {
        boolean shouldInclude(
                Group group,
                Artifact artifact
        );

        ExclusionData join(ExclusionData other);
        ExclusionData meet(ExclusionData other);

        Tuple4<
                Boolean,
                Set<Group>,
                Set<Artifact>,
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
        public boolean shouldInclude(Group group, Artifact artifact) {
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
        public Tuple4<Boolean, Set<Group>, Set<Artifact>, Set<Exclusion>> partitioned() {
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
        public boolean shouldInclude(Group group, Artifact artifact) {
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
        public Tuple4<Boolean, Set<Group>, Set<Artifact>, Set<Exclusion>> partitioned() {
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
            return Set.of(new Exclusion(Group.ALL, Artifact.ALL));
        }

        @Override
        public String toString() {
            return "ExcludeAll";
        }
    }

    public record ExcludeSpecific(
            Set<Group> byGroup,
            Set<Artifact> byArtifact,
            Set<Exclusion> specific
    ) implements ExclusionData {
        public ExcludeSpecific(
                Set<Group> byGroup,
                Set<Artifact> byArtifact,
                Set<Exclusion> specific
        ) {
            this.byGroup = Set.copyOf(byGroup);
            this.byArtifact = Set.copyOf(byArtifact);
            this.specific = Set.copyOf(specific);
        }

        @Override
        public boolean shouldInclude(Group group, Artifact artifact) {
            return !this.byGroup.contains(group)
                    && !this.byArtifact.contains(artifact)
                    && !this.specific.contains(new Exclusion(group, artifact));
        }

        @Override
        public ExclusionData join(ExclusionData other) {
            return switch (other) {
                case ExcludeNone __ -> this;
                case ExcludeAll all -> all;
                case ExcludeSpecific(
                        Set<Group> otherByOrg,
                        Set<Artifact> otherByArtifact,
                        Set<Exclusion> otherSpecific
                ) -> {

                    var joinedByOrg = new HashSet<Group>();
                    joinedByOrg.addAll(this.byGroup);
                    joinedByOrg.addAll(otherByOrg);

                    var joinedByModule = new HashSet<Artifact>();
                    joinedByModule.addAll(this.byArtifact);
                    joinedByModule.addAll(otherByArtifact);

                    var joinedSpecific = new HashSet<Exclusion>();
                    this.specific
                            .stream()
                            .filter(exclusion ->
                                    !otherByOrg.contains(exclusion.group()) &&
                                        !otherByArtifact.contains(exclusion.artifact()))
                            .forEach(joinedSpecific::add);

                    otherSpecific
                            .stream()
                            .filter(exclusion ->
                                    !byGroup.contains(exclusion.group()) &&
                                            !byArtifact.contains(exclusion.artifact()))
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
                        Set<Group> otherByOrg,
                        Set<Artifact> otherByModule,
                        Set<Exclusion> otherSpecific
                )  -> {
                    var metByOrg = byGroup.stream()
                            .filter(otherByOrg::contains)
                            .collect(Collectors.toUnmodifiableSet());

                    var metByModule = byArtifact.stream()
                            .filter(otherByModule::contains)
                            .collect(Collectors.toUnmodifiableSet());

                    var metSpecific = new HashSet<Exclusion>();
                    specific.stream()
                            .filter(exclusion -> {
                                var org = exclusion.group();
                                var moduleName = exclusion.artifact();
                                return otherByOrg.contains(org) ||
                                        otherByModule.contains(moduleName) ||
                                        otherSpecific.contains(exclusion);
                            })
                            .forEach(metSpecific::add);

                    otherSpecific.stream()
                            .filter(exclusion -> {
                                var org = exclusion.group();
                                var moduleName = exclusion.artifact();
                                return byGroup.contains(org) ||
                                        byArtifact.contains(moduleName) ||
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
        public Tuple4<Boolean, Set<Group>, Set<Artifact>, Set<Exclusion>> partitioned() {
            return new Tuple4<>(
                    false,
                    byGroup,
                    byArtifact,
                    specific
            );
        }

        @Override
        public ExclusionData map(Function<String, String> f) {
            return new ExcludeSpecific(
                    byGroup.stream()
                            .map(org -> org.map(f))
                            .collect(Collectors.toUnmodifiableSet()),
                    byArtifact.stream()
                            .map(moduleName -> moduleName.map(f))
                            .collect(Collectors.toUnmodifiableSet()),
                    specific.stream()
                            .map(exclusion -> new Exclusion(
                                    exclusion.group().map(f),
                                    exclusion.artifact().map(f)
                            ))
                            .collect(Collectors.toUnmodifiableSet())
            );
        }

        @Override
        public int size() {
            return byGroup.size() + byArtifact().size() + specific.size();
        }

        @Override
        public boolean subsetOf(ExclusionData other) {
            return switch (other) {
                case ExcludeNone __ -> false;
                case ExcludeAll __ -> true;
                case ExcludeSpecific excludeSpecific ->
                        excludeSpecific.byGroup.containsAll(byGroup)
                        && excludeSpecific.byArtifact.containsAll(byArtifact)
                        && excludeSpecific.specific.containsAll(specific);
            };
        }

        @Override
        public Set<Exclusion> toSet() {
            var set = new HashSet<Exclusion>();
            byGroup.stream()
                    .map(org -> new Exclusion(org, Artifact.ALL))
                    .forEach(set::add);
            byArtifact.stream()
                    .map(moduleName -> new Exclusion(Group.ALL, moduleName))
                    .forEach(set::add);
            set.addAll(specific);

            return Set.copyOf(set);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return (this == obj) || (
                obj instanceof Exclusions exclusions &&
                this.exclusionData.equals(exclusions.exclusionData)
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
        return exclusionData.toString();
    }
}
