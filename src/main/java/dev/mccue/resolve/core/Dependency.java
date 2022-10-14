package dev.mccue.resolve.core;

import dev.mccue.resolve.doc.Coursier;
import dev.mccue.resolve.doc.Incomplete;
import dev.mccue.resolve.util.Tuple2;

import java.util.Objects;
import java.util.Set;

@Incomplete
@Coursier("https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/core/Dependency.scala")
public record Dependency(
        Module module,
        String version,
        Configuration configuration,
        MinimizedExclusions minimizedExclusions,
        Publication publication,
        boolean optional,
        boolean transitive
) {
    public Dependency(
            Module module,
            String version,
            Configuration configuration,
            MinimizedExclusions minimizedExclusions,
            Publication publication,
            boolean optional,
            boolean transitive
    ) {
        this.module = Objects.requireNonNull(
                module,
                "module must not be null");
        this.version = Objects.requireNonNull(
                version,
                "version must not be null"
        );
        this.configuration = Objects.requireNonNull(
                configuration,
                "configuration must not be null"
        );
        this.minimizedExclusions = Objects.requireNonNull(
                minimizedExclusions,
                "minimizedExclusions must not be null"
        );
        this.publication = Objects.requireNonNull(
                publication,
                "publication must not be null"
        );
        this.optional = optional;
        this.transitive = transitive;
    }

    public Dependency(
            Module module,
            String version
    ) {
        this(
                module,
                version,
                Configuration.EMPTY,
                MinimizedExclusions.ZERO,
                Publication.EMPTY,
                false,
                true
        );
    }

    public Dependency(
            Module module,
            String version,
            Configuration configuration,
            Set<Tuple2<Organization, ModuleName>> exclusions,
            Attributes attributes,
            boolean optional,
            boolean transitive
    ) {
        this(
                module,
                version,
                configuration,
                MinimizedExclusions.of(exclusions),
                attributes.publication("", Extension.EMPTY),
                optional,
                transitive
        );
    }
}
