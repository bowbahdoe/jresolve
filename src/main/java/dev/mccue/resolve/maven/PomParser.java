package dev.mccue.resolve.maven;

import dev.mccue.resolve.*;
import dev.mccue.resolve.doc.Coursier;
import dev.mccue.resolve.util.LL;
import dev.mccue.resolve.util.Tuple2;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class PomParser extends DefaultHandler {
    final State state = new State();
    LL<String> paths = new LL.Nil<>();

    // Each "path" in the document has potentially a registered handler.
    // If it doesn't have one, we still want to track it so popping and
    LL<Optional<Handler>> handlers = new LL.Nil<>();

    // handlers
    StringBuilder characterBuffer = new StringBuilder();

    // dependencies, dependency, artifactId
    @Override
    public void startElement(String _uri, String _localName, String tagName, org.xml.sax.Attributes _attributes)  {
        var paths = this.paths.prepend(tagName);
        this.paths = paths;
        var handler = HANDLER_MAP.getOrDefault(
                this.paths,
                HANDLER_MAP.get(paths.tail().prepend("*"))
        );

        this.handlers = this.handlers.prepend(Optional.ofNullable(handler));

        if (handler != null) {
            switch (handler) {
                case ContentHandler __ -> {
                }
                case SectionHandler s ->
                        s.start(state);
                case PropertyHandler p ->
                        p.name(state, tagName);
            }
        }

    }

    @Override
    public void characters(char[] ch, int start, int length) {
        boolean readContent = this.handlers.headOption()
                .flatMap(handlerOpt -> handlerOpt.map(handler ->
                        switch (handler) {
                            case PropertyHandler __ -> true;
                            case ContentHandler __ -> true;
                            case SectionHandler __ -> false;
                        }
                ))
                .orElse(false);

        if (readContent) {
            characterBuffer.append(ch, start, length);
        }
    }

    @Override
    public void endElement(String _uri, String _localName, String tagName) {
        var handlerOpt = handlers.headOption().flatMap(Function.identity());
        // Calling endElement implies startElement was called,
        // which means a non-empty paths and handlers
        this.paths = this.paths.assumeNotEmpty().tail();
        this.handlers = this.handlers.assumeNotEmpty().tail();

        handlerOpt.ifPresent(handler -> {
            switch (handler) {
                case PropertyHandler p ->
                        p.content(state, characterBuffer.toString());
                case ContentHandler c ->
                        c.content(state, characterBuffer.toString());
                case SectionHandler s ->
                        s.end(state);
            }
        });

        characterBuffer.setLength(0);
    }

    private static final class State {
        String groupId = "";
        Optional<String> artifactIdOpt = Optional.empty();
        String version = "";
        Optional<String> parentGroupIdOpt = Optional.empty();
        Optional<String> parentArtifactIdOpt = Optional.empty();
        String parentVersion = "";

        String description = "";
        String url = "";


        Optional<Type> packagingOpt = Optional.empty();
        final ArrayList<Tuple2<Scope, PomDependency>> dependencies = new ArrayList<>();
        final ArrayList<Tuple2<Scope, PomDependency>> dependencyManagement = new ArrayList<>();

        final ArrayList<Tuple2<String, String>> properties = new ArrayList<>();

        Optional<Group> relocationGroupIdOpt = Optional.empty();
        Optional<Artifact> relocationArtifactIdOpt = Optional.empty();
        Optional<String> relocationVersionOpt = Optional.empty();

        Optional<Group> dependencyGroupIdOpt = Optional.empty();
        Optional<Artifact> dependencyArtifactIdOpt = Optional.empty();
        String dependencyVersion = "";
        boolean dependencyOptional = false;
        Scope dependencyScope = Scope.EMPTY;
        Type dependencyType = Type.EMPTY;
        Classifier dependencyClassifier = Classifier.EMPTY;
        final HashSet<Exclusion> dependencyExclusions = new HashSet<>();

        Group dependencyExclusionGroupId = Group.ALL;
        Artifact dependencyExclusionArtifactId = Artifact.ALL;

        Optional<String> propertyNameOpt = Optional.empty();

        String profileId = "";
        final ArrayList<Tuple2<Scope, PomDependency>> profileDependencies = new ArrayList<>();
        final ArrayList<Tuple2<Scope, PomDependency>> profileDependencyManagement = new ArrayList<>();
        final HashMap<String, String> profileProperties = new HashMap<>();
        Optional<Boolean> profileActiveByDefaultOpt = Optional.empty();

        final ArrayList<Profile> profiles = new ArrayList<>();

        PomInfo pomInfo() {
            final Optional<String> groupIdOpt;
            if (!groupId.isEmpty()) {
                groupIdOpt = Optional.of(groupId);
            } else {
                groupIdOpt = parentGroupIdOpt.filter(s -> !s.isEmpty());
            }

            final Optional<String> versionOpt;
            if (!version.isEmpty()) {
                versionOpt = Optional.of(version);
            } else {
                versionOpt = Optional.of(parentVersion).filter(s -> !s.isEmpty());
            }

            var properties0 = List.copyOf(properties);

            final Optional<Library> parentModuleOpt;
            {
                var parentGroupId = parentGroupIdOpt.orElse(null);
                var parentArtifactId = parentArtifactIdOpt.orElse(null);
                if (parentGroupId != null && parentArtifactId != null) {
                    parentModuleOpt = Optional.of(new Library(
                            new Group(parentGroupId),
                            new Artifact(parentArtifactId)
                    ));
                }
                else {
                    parentModuleOpt = Optional.empty();
                }
            }

            var finalGroupId = groupIdOpt.orElseThrow(() -> new RuntimeException("No groupId found"));
            var artifactId = artifactIdOpt.orElseThrow(() -> new RuntimeException("No artifactId found"));
            var finalVersion = versionOpt.orElseThrow(() -> new RuntimeException("No version found"));

            var parentModule = parentModuleOpt.orElse(null);
            if (parentModule != null && parentModule.group().value().isEmpty()) {
                throw new RuntimeException("Parent organization missing");
            }

            if (parentModule != null && parentVersion.isEmpty()) {
                throw new RuntimeException("No parent version found");
            }

            var parentOpt = parentModuleOpt.map(module -> new Tuple2<>(
                    module, parentVersion
            ));


            var projModule = new Library(finalGroupId, artifactId);

            return new PomInfo(
                    projModule,
                    finalVersion,
                    List.copyOf(dependencies),
                    parentOpt,
                    List.copyOf(dependencyManagement),
                    properties0,
                    List.copyOf(profiles),
                    packagingOpt
            );
        }
    }

    private sealed interface Handler {
        LL<String> path();
    }

    private non-sealed interface SectionHandler extends Handler {
        void start(State state);
        void end(State state);
    }

    private non-sealed interface ContentHandler extends Handler {
        void content(State state, String content);
    }

    private non-sealed interface PropertyHandler extends Handler {
        void name(State state, String name);
        void content(State state, String content);
    }

    private interface AddDepHandler {
        void add(State state, Scope scope, PomDependency dependency);
    }

    private static List<Handler> dependencyHandlers(
            LL<String> prefix,
            AddDepHandler addDepHandler
    ) {
        return List.of(
                new SectionHandler() {
                    @Override
                    public void start(State state) {
                        state.dependencyGroupIdOpt = Optional.empty();
                        state.dependencyArtifactIdOpt = Optional.empty();
                        state.dependencyVersion = "";
                        state.dependencyOptional = false;
                        state.dependencyScope = Scope.EMPTY;
                        state.dependencyType = Type.EMPTY;
                        state.dependencyClassifier = Classifier.EMPTY;
                        state.dependencyExclusions.clear();
                    }

                    @Override
                    public void end(State state) {
                        var dependency = new PomDependency(
                                new Library(
                                        state.dependencyGroupIdOpt.orElseThrow(() -> new RuntimeException(
                                                "Expected a groupId on dependency"
                                        )),
                                        state.dependencyArtifactIdOpt.orElseThrow(() -> new RuntimeException(
                                                "Expected an artifactId on dependency"
                                        ))
                                ),
                                state.dependencyVersion,
                                state.dependencyExclusions,
                                state.dependencyType,
                                state.dependencyClassifier,
                                state.dependencyOptional
                        );
                        addDepHandler.add(
                                state,
                                state.dependencyScope,
                                dependency
                        );
                    }

                    @Override
                    public LL<String> path() {
                        return prefix;
                    }
                },
                content(
                        new LL.Cons<>("groupId", prefix),
                        (state, content) ->
                                state.dependencyGroupIdOpt =
                                        Optional.of(new Group(content))
                ),
                content(
                        new LL.Cons<>("artifactId", prefix),
                        (state, content) ->
                                state.dependencyArtifactIdOpt =
                                        Optional.of(new Artifact(content))
                ),
                content(
                        new LL.Cons<>("version", prefix),
                        (state, content) ->
                                state.dependencyVersion = content
                ),
                content(
                        new LL.Cons<>("optional", prefix),
                        (state, content) ->
                                state.dependencyOptional = "true".equals(content)
                ),
                content(
                        new LL.Cons<>("scope", prefix),
                        (state, content) ->
                                state.dependencyScope = new Scope(content)
                ),
                content(
                        new LL.Cons<>("type", prefix),
                        (state, content) ->
                                state.dependencyType = new Type(content)
                ),
                content(
                        new LL.Cons<>("classifier", prefix),
                        (state, content) ->
                                state.dependencyClassifier = new Classifier(content)
                ),
                new SectionHandler() {
                    @Override
                    public void start(State state) {
                        state.dependencyExclusionGroupId = Group.ALL;
                        state.dependencyExclusionArtifactId = Artifact.ALL;
                    }

                    @Override
                    public void end(State state) {
                        state.dependencyExclusions.add(
                               new Exclusion(
                                       state.dependencyExclusionGroupId,
                                       state.dependencyExclusionArtifactId
                               )
                        );
                    }

                    @Override
                    public LL<String> path() {
                        return new LL.Cons<>("exclusion", new LL.Cons<>("exclusions", prefix));
                    }
                },
                content(
                        new LL.Cons<>("groupId", new LL.Cons<>("exclusion", new LL.Cons<>("exclusions", prefix))),
                        (state, content) ->
                                state.dependencyExclusionGroupId = new Group(content)
                ),
                content(
                        new LL.Cons<>("artifactId", new LL.Cons<>("exclusion", new LL.Cons<>("exclusions", prefix))),
                        (state, content) ->
                                state.dependencyExclusionArtifactId = new Artifact(content)
                )
        );
    }

    private interface AddPropertyHandler {
        void add(State state, String key, String value);
    }

    private static List<Handler> propertyHandlers(
            LL<String> prefix,
            AddPropertyHandler add
    ) {
        return List.of(
                new PropertyHandler() {
                    @Override
                    public void name(State state, String name) {
                        state.propertyNameOpt = Optional.of(name);
                    }

                    @Override
                    public void content(State state, String content) {
                        add.add(
                                state,
                                state.propertyNameOpt.orElseThrow(() ->
                                        new IllegalStateException("Property name should have been set by the name method")
                                ),
                                content
                        );
                        state.propertyNameOpt = Optional.empty();
                    }

                    @Override
                    public LL<String> path() {
                        return new LL.Cons<>("*", prefix);
                    }
                }
        );
    }

    private interface AddProfileHandler {
        void add(State state, Profile profile);
    }

    private static List<Handler> profileHandlers(
            LL<String> prefix,
            AddProfileHandler add
    ) {
        var handlers = new ArrayList<>(List.of(
                new SectionHandler() {
                    @Override
                    public void start(State state) {
                        state.profileId = "";
                        state.profileActiveByDefaultOpt = Optional.empty();
                        state.profileDependencies.clear();
                        state.profileDependencyManagement.clear();
                        state.profileProperties.clear();
                    }

                    @Override
                    public void end(State state) {
                        var profile = new Profile(
                                state.profileId,
                                state.profileActiveByDefaultOpt,
                                List.copyOf(state.profileDependencies),
                                List.copyOf(state.profileDependencyManagement),
                                Map.copyOf(state.profileProperties)
                        );

                        add.add(state, profile);
                    }

                    @Override
                    public LL<String> path() {
                        return prefix;
                    }
                },
                content(
                        new LL.Cons<>("id", prefix),
                        (state, content) ->
                                state.profileId = content
                ),
                content(
                        new LL.Cons<>("activeByDefault", new LL.Cons<>("activation", new LL.Nil<>())),
                        (state, content) ->
                                state.profileActiveByDefaultOpt = switch (content) {
                                    case "true" -> Optional.of(true);
                                    case "false" -> Optional.of(false);
                                    default -> Optional.empty();
                                }
                )
        ));

        handlers.addAll(dependencyHandlers(
                new LL.Cons<>("dependency", new LL.Cons<>("dependencies", prefix)),
                (state, configuration, dependency) ->
                        state.profileDependencies.add(new Tuple2<>(configuration, dependency))
        ));

        handlers.addAll(dependencyHandlers(
                new LL.Cons<>("dependency", new LL.Cons<>("dependencies", new LL.Cons<>("dependencyManagement", prefix))),
                (state, configuration, dependency) ->
                        state.profileDependencyManagement.add(new Tuple2<>(configuration, dependency))
        ));

        handlers.addAll(propertyHandlers(
                new LL.Cons<>("properties", prefix),
                (state, key, value) ->
                        state.profileProperties.put(key, value)
        ));

        return handlers;
    }

    private static Handler content(List<String> path, BiConsumer<State, String> callback) {
        return content(LL.fromJavaList(path), callback);
    }

    private static Handler content(LL<String> path, BiConsumer<State, String> callback) {
        return new ContentHandler() {
            @Override
            public void content(State state, String content) {
                callback.accept(state, content);
            }

            @Override
            public LL<String> path() {
                return path;
            }
        };
    }

    private static final Map<LL<String>, Handler> HANDLER_MAP;

    static {
        List<Handler> handlers = new ArrayList<>();
        handlers.addAll(List.of(
                content(
                        List.of("groupId", "project"),
                        (state, content) ->
                                state.groupId = content
                ),
                content(
                        List.of("artifactId", "project"),
                        (state, content) ->
                                state.artifactIdOpt = Optional.of(content)
                ),
                content(
                        List.of("version", "project"),
                        (state, content) ->
                                state.version = content
                ),
                content(
                        List.of("groupId", "parent", "project"),
                        (state, content) ->
                                state.parentGroupIdOpt = Optional.of(content)
                ),
                content(
                        List.of("artifactId", "parent", "project"),
                        (state, content) ->
                                state.parentArtifactIdOpt = Optional.of(content)
                ),
                content(
                        List.of("version", "parent", "project"),
                        (state, content) ->
                                state.parentVersion = content
                ),
                content(
                        List.of("description", "project"),
                        (state, content) ->
                                state.description = content
                ),

                content(
                        List.of("url", "project"),
                        (state, content) ->
                                state.url = content
                ),

                content(
                        List.of("packaging", "project"),
                        (state, content) ->
                                state.packagingOpt = Optional.of(new Type(content))
                ),
                content(
                        List.of("groupId", "relocation", "distributionManagement", "project"),
                        (state, content) ->
                                state.relocationGroupIdOpt = Optional.of(
                                        new Group(content)
                                )
                ),
                content(
                        List.of("artifactId", "relocation", "distributionManagement", "project"),
                        (state, content) ->
                                state.relocationArtifactIdOpt = Optional.of(
                                        new Artifact(content)
                                )
                ),
                content(
                        List.of("version", "relocation", "distributionManagement", "project"),
                        (state, content) ->
                                state.relocationVersionOpt = Optional.of(content)
                )
        ));

        handlers.addAll(dependencyHandlers(
                LL.fromJavaList(List.of("dependency", "dependencies", "project")),
                (state, configuration, dependency) ->
                        state.dependencies.add(new Tuple2<>(configuration, dependency))
        ));

        handlers.addAll(dependencyHandlers(
                LL.fromJavaList(List.of("dependency", "dependencies", "dependencyManagement", "project")),
                (state, configuration, dependency) ->
                        state.dependencyManagement.add(new Tuple2<>(configuration, dependency))
        ));

        handlers.addAll(propertyHandlers(
                LL.fromJavaList(List.of("properties", "project")),
                (state, key, value) ->
                        state.properties.add(new Tuple2<>(key, value))
        ));

        handlers.addAll(profileHandlers(
                LL.fromJavaList(List.of("profile", "profiles", "project")),
                (state, profile) ->
                        state.profiles.add(profile)
        ));
        HANDLER_MAP = handlers.stream()
                .collect(Collectors.toUnmodifiableMap(
                        Handler::path,
                        handler -> handler
                ));
    }

    public PomInfo pomInfo() {
        return this.state.pomInfo();
    }

    public static PomInfo parse(String pomString) {
        var pomParser = new PomParser();
        var factory = SAXParserFactory.newDefaultInstance();
        try {
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(
                    new ByteArrayInputStream(pomString.getBytes(StandardCharsets.UTF_8)),
                    pomParser
            );
        } catch (ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        return pomParser.pomInfo();
    }
}
