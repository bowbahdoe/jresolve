package dev.mccue.resolve.maven;

import dev.mccue.resolve.core.Classifier;
import dev.mccue.resolve.core.Extension;
import dev.mccue.resolve.core.Type;
import dev.mccue.resolve.doc.Coursier;
import dev.mccue.resolve.util.Tuple2;

import java.util.Map;
import java.util.Optional;

@Coursier("https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/maven/MavenAttributes.scala")
public final class MavenAttributes {
    private MavenAttributes() {}

    private static final Map<Type, Extension> TYPE_EXTENSIONS = Map.ofEntries(
            Map.entry(new Type("eclipse-plugin"), Extension.JAR),
            Map.entry(new Type("maven-plugin"), Extension.JAR),
            Map.entry(new Type("hk2-jar"),  Extension.JAR),
            Map.entry(new Type("orbit"), Extension.JAR),
            Map.entry(new Type("scala-jar"), Extension.JAR),
            Map.entry(Type.JAR, Extension.JAR),
            Map.entry(Type.BUNDLE, Extension.JAR),
            Map.entry(new Type("doc"), Extension.JAR),
            Map.entry(new Type("src"),  Extension.JAR),
            Map.entry( Type.TEST_JAR, Extension.JAR),
            Map.entry(new Type("ejb-client"), Extension.JAR)
    );

    public static Extension typeExtension(Type type) {
        return TYPE_EXTENSIONS.getOrDefault(type, type.asExtension());
    }

    private static final Map<Type, Classifier> TYPE_DEFAULT_CLASSIFIERS = Map.of(
            Type.TEST_JAR, Classifier.TESTS,
            Type.JAVADOC, Classifier.JAVADOC,
            Type.JAVA_SOURCE, Classifier.SOURCES,
            new Type("ejb-client"), new Classifier("client")
    );

    public static Optional<Classifier> typeDefaultClassifierOpt(Type type) {
        return Optional.ofNullable(TYPE_DEFAULT_CLASSIFIERS.get(type));
    }

    public static Classifier typeDefaultClassifier(Type type) {
        return typeDefaultClassifierOpt(type).orElse(Classifier.EMPTY);
    }

    private static Map<Tuple2<Classifier, Extension>, Type> CLASSIFIER_EXTENSION_DEFAULT_TYPES = Map.of(
            new Tuple2<>(Classifier.TESTS, Extension.JAR), Type.TEST_JAR,
            new Tuple2<>(Classifier.JAVADOC, Extension.JAR), Type.DOC,
            new Tuple2<>(Classifier.SOURCES, Extension.JAR), Type.SOURCE
    );

    public static Optional<Type> classifierExtensionDefaultTypeOpt(
            Classifier classifier,
            Extension extension
    ) {
        return Optional.ofNullable(
                CLASSIFIER_EXTENSION_DEFAULT_TYPES.get(new Tuple2<>(classifier, extension))
        );
    }


}
