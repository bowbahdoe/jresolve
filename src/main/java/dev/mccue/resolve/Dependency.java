package dev.mccue.resolve;

import dev.mccue.purl.PackageUrl;
import dev.mccue.resolve.maven.Classifier;
import dev.mccue.resolve.maven.MavenCoordinate;
import dev.mccue.resolve.maven.MavenRepository;
import dev.mccue.resolve.maven.Scope;

import java.util.*;

public record Dependency(
        Library library,
        Coordinate coordinate,
        Exclusions exclusions,
        Usages usages
) {
    public Dependency(
            Library library,
            Coordinate coordinate,
            Exclusions exclusions,
            Usages usages
    ) {
        this.library = Objects.requireNonNull(library, "library must not be null.");
        this.coordinate = Objects.requireNonNull(coordinate, "coordinate must not be null.");
        this.exclusions = Objects.requireNonNull(exclusions, "exclusions must not be null.");
        this.usages = Objects.requireNonNull(usages, "usages must not be null");
    }

    public Dependency(Library library, Coordinate coordinate) {
        this(library, coordinate, Exclusions.NONE);
    }

    public Dependency(Library library, Coordinate coordinate, Usages usages) {
        this(library, coordinate, Exclusions.NONE, usages);
    }

    public Dependency(Library library, Coordinate coordinate, Exclusions exclusions) {
        this(library, coordinate, exclusions, Usages.unspecified());
    }

    public static Dependency maven(
            Group group,
            Artifact artifact,
            Version version,
            List<MavenRepository> repositories
    ) {
        return new Dependency(
                new Library(group, artifact),
                new MavenCoordinate(
                        group,
                        artifact,
                        version,
                        repositories
                )
        );
    }

    public static Dependency maven(
            Group group,
            Artifact artifact,
            Version version,
            MavenRepository repository
    ) {
        return maven(group, artifact, version, List.of(repository));
    }

    public static Dependency maven(
            String group,
            String artifact,
            String version,
            List<MavenRepository> repositories
    ) {
        return maven(
                new Group(group),
                new Artifact(artifact),
                new Version(version),
                repositories
        );
    }

    public static Dependency maven(
            String group,
            String artifact,
            String version,
            MavenRepository repository
    ) {
        return maven(group, artifact, version, List.of(repository));
    }

    public static Dependency mavenCentral(Group group, Artifact artifact, Version version) {
        return maven(
                group,
                artifact,
                version,
                List.of(MavenRepository.central())
        );
    }

    public static Dependency mavenCentral(String group, String artifact, String version) {
        return maven(
                new Group(group),
                new Artifact(artifact),
                new Version(version),
                List.of(MavenRepository.central())
        );
    }

    public static Dependency fromPackageUrl(
            PackageUrl packageUrl
    ) {
        return fromPackageUrl(packageUrl, Map.of("central", MavenRepository.central()));
    }

    public static Dependency fromPackageUrl(
            PackageUrl packageUrl,
            Map<String, MavenRepository> knownRepositories
    ) {
        return fromPackageUrl(packageUrl, knownRepositories, Usages.unspecified());
    }


    public static Dependency fromPackageUrl(
            PackageUrl packageUrl,
            Map<String, MavenRepository> knownRepositories,
            Usages defaultUsage
    ) {
        if (!packageUrl.getType().equals("maven")) {
            throw new IllegalArgumentException(packageUrl.getType() + " is not a supported package url type.");
        }

        var group = new Group(String.join(".", Objects.requireNonNull(packageUrl.getNamespace(), "Package url must have a namespace")));
        var artifact = new Artifact(packageUrl.getName());
        var version = new Version(Objects.requireNonNull(packageUrl.getVersion(), "Package url must have a version"));

        var repositoryNames = List.of("central");
        var usage = defaultUsage;
        String classifierStr = null;

        var qualifiers = packageUrl.getQualifiers();
        if (qualifiers != null) {
            var repoQualifier = qualifiers.get("repository");
            if (repoQualifier != null) {
                repositoryNames = Arrays.asList(repoQualifier.split(","));
            }

            var classifierQualifier = qualifiers.get("classifier");
            if (classifierQualifier != null && !classifierQualifier.equals("default")) {
                classifierStr = classifierQualifier;
            }

            var usageQualifier = qualifiers.get("usage");
            if (usageQualifier != null) {
                var usagesArray = usageQualifier.split(",");
                var usageList = new ArrayList<Usage>();
                Arrays.stream(usagesArray).map(Usage::new).forEach(usageList::add);
                usage = Usages.specified(usageList);
            }
        }

        var repositories = new ArrayList<MavenRepository>();
        for (var repositoryName : repositoryNames) {
            var repo = knownRepositories.get(repositoryName);
            if (repo == null) {
                throw new IllegalArgumentException("Unknown repository: " + repositoryName);
            }
            repositories.add(repo);
        }


        var library = new Library(
                group,
                artifact,
                classifierStr == null ? Variant.DEFAULT : new Variant(classifierStr)
        );

        var classifier = classifierStr == null ? Classifier.EMPTY : new Classifier(classifierStr);

        var coordinate = new MavenCoordinate(
                group,
                artifact,
                version,
                List.copyOf(repositories),
                List.of(Scope.COMPILE, Scope.RUNTIME),
                classifier,
                Classifier.SOURCES,
                Classifier.JAVADOC
        );

        return new Dependency(library, coordinate, usage);
    }

    public Dependency withExclusions(Exclusions exclusions) {
        return new Dependency(library, coordinate, exclusions);
    }
}
