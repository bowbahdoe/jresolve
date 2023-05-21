package dev.mccue.resolve;

import dev.mccue.resolve.doc.Coursier;
import dev.mccue.resolve.doc.Gold;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Gold
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
            Exclusion... exclusions
    ) {
        return of(Arrays.asList(exclusions));
    }

    public static Exclusions of(
            List<Exclusion> exclusions
    ) {
        if (exclusions.isEmpty()) {
            return NONE;
        }

        var excludeByGroup0 = new HashSet<Group>();
        var excludeByArtifact0 = new HashSet<Artifact>();
        var remaining0 = new HashSet<Exclusion>();

        for (var exclusion : exclusions) {
            if (Group.ALL.equals(exclusion.group())) {
                if (Artifact.ALL.equals(exclusion.artifact())) {
                    return ALL;
                } else {
                    excludeByArtifact0.add(exclusion.artifact());
                }
            } else if (Artifact.ALL.equals(exclusion.artifact())) {
                excludeByGroup0.add(exclusion.group());
            } else {
                remaining0.add(exclusion);
            }
        }

        return new Exclusions(new ExcludeSpecific(
                Set.copyOf(excludeByGroup0),
                Set.copyOf(excludeByArtifact0),
                remaining0.stream()
                        .filter(exclusion ->
                                !excludeByGroup0.contains(exclusion.group())
                                    && !excludeByArtifact0.contains(exclusion.artifact()))
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

    public int size() {
        return this.exclusionData.size();
    }

    public boolean subsetOf(Exclusions other) {
        return this.exclusionData.subsetOf(other.exclusionData);
    }

    public Set<Exclusion> toSet() {
        return this.exclusionData.toSet();
    }

    sealed interface ExclusionData {
        boolean shouldInclude(
                Group group,
                Artifact artifact
        );

        ExclusionData join(ExclusionData other);
        ExclusionData meet(ExclusionData other);

        ExclusionData map(Function<String, String> f);

        int size();

        boolean subsetOf(ExclusionData other);

        Set<Exclusion> toSet();
    }

    enum ExcludeNone implements ExclusionData {
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

    enum ExcludeAll implements ExclusionData {
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

    record ExcludeSpecific(
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

                    var joinedByGroup = new HashSet<Group>();
                    joinedByGroup.addAll(this.byGroup);
                    joinedByGroup.addAll(otherByOrg);

                    var joinedByArtifact = new HashSet<Artifact>();
                    joinedByArtifact.addAll(this.byArtifact);
                    joinedByArtifact.addAll(otherByArtifact);

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
                            Set.copyOf(joinedByGroup),
                            Set.copyOf(joinedByArtifact),
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
                        Set<Group> otherByGroup,
                        Set<Artifact> otherByArtifact,
                        Set<Exclusion> otherSpecific
                )  -> {
                    var metByGroup = byGroup.stream()
                            .filter(otherByGroup::contains)
                            .collect(Collectors.toUnmodifiableSet());

                    var metByArtifact = byArtifact.stream()
                            .filter(otherByArtifact::contains)
                            .collect(Collectors.toUnmodifiableSet());

                    var metSpecific = new HashSet<Exclusion>();
                    specific.stream()
                            .filter(exclusion -> {
                                var group = exclusion.group();
                                var artifact = exclusion.artifact();
                                return otherByGroup.contains(group) ||
                                        otherByArtifact.contains(artifact) ||
                                        otherSpecific.contains(exclusion);
                            })
                            .forEach(metSpecific::add);

                    otherSpecific.stream()
                            .filter(exclusion -> {
                                var group = exclusion.group();
                                var artifact = exclusion.artifact();
                                return byGroup.contains(group) ||
                                        byArtifact.contains(artifact) ||
                                        specific.contains(exclusion);
                            })
                            .forEach(metSpecific::add);

                    if (metByGroup.isEmpty() && metByArtifact.isEmpty() && metSpecific.isEmpty()) {
                        yield ExcludeNone.INSTANCE;
                    }
                    else {
                        yield new ExcludeSpecific(
                                metByGroup,
                                metByArtifact,
                                Set.copyOf(metSpecific)
                        );
                    }


                }
            };
        }

        @Override
        public ExclusionData map(Function<String, String> f) {
            return new ExcludeSpecific(
                    byGroup.stream()
                            .map(group -> group.map(f))
                            .collect(Collectors.toUnmodifiableSet()),
                    byArtifact.stream()
                            .map(artifact -> artifact.map(f))
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
                    .map(group -> new Exclusion(group, Artifact.ALL))
                    .forEach(set::add);
            byArtifact.stream()
                    .map(artifact -> new Exclusion(Group.ALL, artifact))
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
