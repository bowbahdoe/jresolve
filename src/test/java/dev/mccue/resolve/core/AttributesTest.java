package dev.mccue.resolve.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public final class AttributesTest {
    @Test
    @DisplayName("Cannot pass null to Attributes constructor")
    public void cannotPassNull() {
        assertThrows(NullPointerException.class,
                () -> new Attributes(null, Classifier.EMPTY),
                "Cannot pass a null type.");

        assertThrows(NullPointerException.class,
                () -> new Attributes(Type.EMPTY, null),
                "Cannot pass a null classifier.");

        assertThrows(NullPointerException.class,
                () -> new Attributes(null, null),
                "Cannot pass a null type and classifier.");
    }

    @Test
    @DisplayName("Attributes is empty only when it has an empty type and classifier")
    public void emptyOnlyWhenBothComponentsEmpty() {
        assertTrue(
                new Attributes(Type.EMPTY, Classifier.EMPTY).isEmpty()
        );
        assertFalse(
                new Attributes(Type.JAR, Classifier.EMPTY).isEmpty()
        );
        assertFalse(
                new Attributes(Type.EMPTY, Classifier.JAVADOC).isEmpty()
        );
        assertFalse(
                new Attributes(Type.DOC, Classifier.SOURCES).isEmpty()
        );
    }

    @Test
    @DisplayName("Empty Attributes has an empty type")
    public void emptyAttributesHasEmptyType() {
        assertEquals(
                Type.EMPTY,
                Attributes.EMPTY.type()
        );
    }


    @Test
    @DisplayName("Empty Attributes has an empty type")
    public void emptyAttributesHasEmptyClassifier() {
        assertEquals(
                Classifier.EMPTY,
                Attributes.EMPTY.classifier()
        );
    }

    @Test
    @DisplayName("Empty type means packaging is jar")
    public void emptyTypeMeansPackagingIsJar() {
        assertEquals(
                Type.JAR,
                new Attributes(Type.EMPTY, Classifier.SOURCES).packaging()
        );
    }

    @Test
    @DisplayName("Non-empty type means packaging is equal to that type")
    public void nonEmptyTypeMeansPackagingIsType() {
        assertEquals(
                Type.JAVADOC,
                new Attributes(Type.JAVADOC, Classifier.SOURCES).packaging()
        );
        assertEquals(
                Type.SOURCE,
                new Attributes(Type.SOURCE, Classifier.EMPTY).packaging()
        );
        assertEquals(
                Type.JAR,
                new Attributes(Type.JAR, Classifier.TESTS).packaging()
        );
    }

    @Test
    @DisplayName("Empty Attributes has empty result from packagingAndClassifier")
    public void emptyAttributesHasEmptyPackagingAndClassifier() {
        assertEquals(
                "",
                Attributes.EMPTY.packagingAndClassifier()
        );
    }

    @Test
    @DisplayName("Only empty Classifier means just type is reported for packagingAndClassifier")
    public void onlyEmptyClassifierMeansPackagingAndClassifierIsPackaging() {
        assertEquals(
                Type.SOURCE.value(),
                new Attributes(
                        Type.SOURCE,
                        Classifier.EMPTY
                ).packagingAndClassifier()
        );

        assertEquals(
                Type.JAR.value(),
                new Attributes(
                        Type.JAR,
                        Classifier.EMPTY
                ).packagingAndClassifier()
        );

        assertEquals(
                Type.JAVADOC.value(),
                new Attributes(
                        Type.JAVADOC,
                        Classifier.EMPTY
                ).packagingAndClassifier()
        );
    }

    @Test
    @DisplayName("packackingAndClassifier joined by colon")
    public void packagingAndClassifierJoinedByColon() {
        assertEquals(
                Type.JAR.value() + ":" + Classifier.JAVADOC.value(),
                new Attributes(
                        Type.EMPTY,
                        Classifier.JAVADOC
                ).packagingAndClassifier()
        );

        assertEquals(
                Type.JAR.value() + ":" + Classifier.JAVADOC.value(),
                new Attributes(
                        Type.JAR,
                        Classifier.JAVADOC
                ).packagingAndClassifier()
        );

        assertEquals(
                Type.JAVADOC.value() + ":" + Classifier.JAVADOC.value(),
                new Attributes(
                        Type.JAVADOC,
                        Classifier.JAVADOC
                ).packagingAndClassifier()
        );

        assertEquals(
                Type.DOC.value() + ":" + Classifier.SOURCES.value(),
                new Attributes(
                        Type.DOC,
                        Classifier.SOURCES
                ).packagingAndClassifier()
        );
    }

    @Test
    @DisplayName("Attributes can turn into a publication")
    public void canTurnIntoPublication() {
        assertEquals(
                new Publication(
                        "abc",
                        Type.BUNDLE,
                        Extension.JAR,
                        Classifier.SOURCES
                ),
                new Attributes(
                        Type.BUNDLE,
                        Classifier.SOURCES
                ).publication("abc", Extension.JAR)
        );
    }

    @Test
    @DisplayName("Attributes can round trip through a publication")
    public void canRoundTripThroughPublication() {
        var attributes = new Attributes(
                Type.BUNDLE,
                Classifier.SOURCES
        );

        assertEquals(
                attributes,
                attributes.publication("abc", Extension.JAR)
                        .attributes()
        );
    }
}