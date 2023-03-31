package dev.mccue.resolve.maven;

import dev.mccue.resolve.*;
import dev.mccue.resolve.doc.Coursier;
import dev.mccue.resolve.doc.Rife;
import dev.mccue.resolve.util.LL;
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

@Coursier("https://github.com/coursier/coursier/blob/b5adafa/modules/core/shared/src/main/scala/coursier/maven/PomParser.scala")
@Rife("https://github.com/rife2/rife2/blob/c915071/lib/src/main/java/rife/bld/dependencies/Xml2MavenPom.java")
final class PomParser extends DefaultHandler {
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

    /**
     * A field in State isn't directly represented in the
     * final PomInfo.
     */
    private @interface Temporary {}

    private static final class State {
        PomGroupId groupId = PomGroupId.Undeclared.INSTANCE;
        PomArtifactId artifactId = PomArtifactId.Undeclared.INSTANCE;
        PomVersion version = PomVersion.Undeclared.INSTANCE;
        PomGroupId parentGroupId = PomGroupId.Undeclared.INSTANCE;
        PomArtifactId parentArtifactId = PomArtifactId.Undeclared.INSTANCE;
        PomVersion parentVersion = PomVersion.Undeclared.INSTANCE;


        PomPackaging packaging = PomPackaging.Undeclared.INSTANCE;
        final ArrayList<PomDependency> dependencies = new ArrayList<>();
        final ArrayList<PomDependency> dependencyManagement = new ArrayList<>();

        final ArrayList<PomProperty> properties = new ArrayList<>();

        @Temporary
        PomGroupId dependencyGroupId = PomGroupId.Undeclared.INSTANCE;

        @Temporary
        PomArtifactId dependencyArtifactId = PomArtifactId.Undeclared.INSTANCE;

        @Temporary
        PomVersion dependencyVersion = PomVersion.Undeclared.INSTANCE;

        @Temporary
        PomOptionality dependencyOptional = PomOptionality.Undeclared.INSTANCE;

        @Temporary
        PomScope dependencyScope = PomScope.Undeclared.INSTANCE;

        @Temporary
        PomType dependencyType = PomType.Undeclared.INSTANCE;

        @Temporary
        PomClassifier dependencyClassifier = PomClassifier.Undeclared.INSTANCE;

        final LinkedHashSet<PomExclusion> dependencyExclusions = new LinkedHashSet<>();

        @Temporary
        PomGroupId dependencyExclusionGroupId = PomGroupId.Undeclared.INSTANCE;

        @Temporary
        PomArtifactId dependencyExclusionArtifactId = PomArtifactId.Undeclared.INSTANCE;

        @Temporary
        String propertyName = null;

        PomInfo pomInfo() {
            PomParent parent;
            if (parentGroupId instanceof PomGroupId.Undeclared && parentArtifactId instanceof PomArtifactId.Undeclared
                && parentVersion instanceof PomVersion.Undeclared) {
                parent = PomParent.Undeclared.INSTANCE;
            }
            else if (!(
                    parentGroupId instanceof PomGroupId.Declared declaredParentGroupId
                            && parentArtifactId instanceof PomArtifactId.Declared declaredParentArtifactId
                            && parentVersion instanceof PomVersion.Declared declaredParentVersion
            )) {
                throw new RuntimeException("Parent must have group, artifact, and version declared");
            }
            else {
                parent = new PomParent.Declared(
                        declaredParentGroupId,
                        declaredParentArtifactId,
                        declaredParentVersion
                );
            }
            return new PomInfo(
                    groupId,
                    artifactId,
                    version,
                    List.copyOf(dependencies),
                    parent,
                    List.copyOf(dependencyManagement),
                    List.copyOf(properties),
                    packaging
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
        void add(State state, PomDependency dependency);
    }

    private static List<Handler> dependencyHandlers(
            LL<String> prefix,
            AddDepHandler addDepHandler
    ) {
        return List.of(
                new SectionHandler() {
                    @Override
                    public void start(State state) {
                        state.dependencyGroupId = PomGroupId.Undeclared.INSTANCE;
                        state.dependencyArtifactId = PomArtifactId.Undeclared.INSTANCE;
                        state.dependencyVersion = PomVersion.Undeclared.INSTANCE;
                        state.dependencyOptional = PomOptionality.Undeclared.INSTANCE;
                        state.dependencyScope = PomScope.Undeclared.INSTANCE;
                        state.dependencyType = PomType.Undeclared.INSTANCE;
                        state.dependencyClassifier = PomClassifier.Undeclared.INSTANCE;
                        state.dependencyExclusions.clear();
                    }

                    @Override
                    public void end(State state) {
                        var dependency = new PomDependency(
                                state.dependencyGroupId,
                                state.dependencyArtifactId,
                                state.dependencyVersion,
                                state.dependencyExclusions,
                                state.dependencyType,
                                state.dependencyClassifier,
                                state.dependencyOptional,
                                state.dependencyScope
                        );
                        addDepHandler.add(
                                state,
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
                        (state, content) -> {
                            state.dependencyGroupId =
                                    new PomGroupId.Declared(content);
                        }
                ),
                content(
                        new LL.Cons<>("artifactId", prefix),
                        (state, content) -> {
                            if (!content.isBlank()) {
                                state.dependencyArtifactId =
                                        new PomArtifactId.Declared(content);
                            }
                        }
                ),
                content(
                        new LL.Cons<>("version", prefix),
                        (state, content) -> {
                            state.dependencyVersion =
                                    new PomVersion.Declared(content);
                        }

                ),
                content(
                        new LL.Cons<>("optional", prefix),
                        (state, content) ->
                                state.dependencyOptional = new PomOptionality.Declared(content)
                ),
                content(
                        new LL.Cons<>("scope", prefix),
                        (state, content) -> state.dependencyScope = new PomScope.Declared(content)
                ),
                content(
                        new LL.Cons<>("type", prefix),
                        (state, content) ->
                                state.dependencyType = new PomType.Declared(content)
                ),
                content(
                        new LL.Cons<>("classifier", prefix),
                        (state, content) ->
                                state.dependencyClassifier = new PomClassifier.Declared(content)
                ),
                new SectionHandler() {
                    @Override
                    public void start(State state) {
                        state.dependencyExclusionGroupId = PomGroupId.Undeclared.INSTANCE;
                        state.dependencyExclusionArtifactId = PomArtifactId.Undeclared.INSTANCE;
                    }

                    @Override
                    public void end(State state) {
                        state.dependencyExclusions.add(
                               new PomExclusion(
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
                        (state, content) -> {
                            state.dependencyExclusionGroupId =
                                    new PomGroupId.Declared(content);
                        }
                ),
                content(
                        new LL.Cons<>("artifactId", new LL.Cons<>("exclusion", new LL.Cons<>("exclusions", prefix))),
                        (state, content) -> {
                            state.dependencyExclusionArtifactId =
                                    new PomArtifactId.Declared(content);
                        }
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
                        state.propertyName = name;
                    }

                    @Override
                    public void content(State state, String content) {
                        add.add(
                                state,
                                Objects.requireNonNull(state.propertyName),
                                content
                        );
                        state.propertyName = null;
                    }

                    @Override
                    public LL<String> path() {
                        return new LL.Cons<>("*", prefix);
                    }
                }
        );
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
                        (state, content) -> {
                            if (!content.isBlank()) {
                                state.groupId = new PomGroupId.Declared(content);
                            }
                        }

                ),
                content(
                        List.of("artifactId", "project"),
                        (state, content) ->{
                            if (!content.isBlank()) {
                                state.artifactId = new PomArtifactId.Declared(content);
                            }
                        }
                ),
                content(
                        List.of("version", "project"),
                        (state, content) ->{
                            if (!content.isBlank()) {
                                state.version = new PomVersion.Declared(content);
                            }
                        }
                ),
                content(
                        List.of("groupId", "parent", "project"),
                        (state, content) -> {
                            if (!content.isBlank()) {
                                state.parentGroupId = new PomGroupId.Declared(content);
                            }
                        }
                ),
                content(
                        List.of("artifactId", "parent", "project"),
                        (state, content) -> {
                            if (!content.isBlank()) {
                                state.parentArtifactId = new PomArtifactId.Declared(content);
                            }
                        }
                ),
                content(
                        List.of("version", "parent", "project"),
                        (state, content) -> {
                            if (!content.isBlank()) {
                                state.parentVersion = new PomVersion.Declared(content);
                            }
                        }
                ),
                content(
                        List.of("packaging", "project"),
                        (state, content) -> {
                            if (!content.isBlank()) {
                                state.packaging = new PomPackaging.Declared(content);
                            }
                        }
                )
        ));

        handlers.addAll(dependencyHandlers(
                LL.fromJavaList(List.of("dependency", "dependencies", "project")),
                (state, dependency) ->
                        state.dependencies.add(dependency)
        ));

        handlers.addAll(dependencyHandlers(
                LL.fromJavaList(List.of("dependency", "dependencies", "dependencyManagement", "project")),
                (state, dependency) ->
                        state.dependencyManagement.add(dependency)
        ));

        handlers.addAll(propertyHandlers(
                LL.fromJavaList(List.of("properties", "project")),
                (state, key, value) ->
                        state.properties.add(new PomProperty(key, value))
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
