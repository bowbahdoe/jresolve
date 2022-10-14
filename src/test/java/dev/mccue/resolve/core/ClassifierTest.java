package dev.mccue.resolve.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public final class ClassifierTest {
    @Test
    @DisplayName("Classifiers are lexicographically sortable")
    public void classifierIsSortableByValue() {
        var classifiers = new ArrayList<>(List.of(
                new Classifier("f"),
                new Classifier("e"),
                new Classifier("a"),
                new Classifier("b"),
                new Classifier("d"),
                new Classifier("c")
        ));

        Collections.sort(classifiers);

        assertEquals(List.of(
                new Classifier("a"),
                new Classifier("b"),
                new Classifier("c"),
                new Classifier("d"),
                new Classifier("e"),
                new Classifier("f")
        ), classifiers);
    }

    @Test
    @DisplayName("The 'empty' classifier is reported as empty by isEmpty")
    public void emptyClassifierIsEmpty() {
        assertTrue(
                Classifier.EMPTY.isEmpty(),
                "empty classifier is empty"
        );
    }

    @Test
    @DisplayName("The 'empty' classifier holds an empty string")
    public void emptyClassifierHoldsEmptyString() {
        assertEquals(
                Classifier.EMPTY.value(),
                ""
        );
    }

    @Test
    @DisplayName("Cant make a Classifier with a null value")
    public void cantMakeNullValuedClassifier() {
        assertThrows(
                NullPointerException.class,
                () -> new Classifier(null),
                "Should not be able to make a Classifier with a null value."
        );
    }

    @Test
    @DisplayName("Can use map to update the value in a Classifier")
    public void mapClassifier() {
        assertEquals(
                new Classifier("ABC"),
                new Classifier("abc").map(String::toUpperCase)
        );
    }

    @Test
    @DisplayName("The result of map cannot be null")
    public void nullMapClassifier() {
        assertThrows(
                NullPointerException.class,
                () -> new Classifier("").map(__ -> null),
                "Should not be able to return null from map."
        );
    }

    @Test
    @DisplayName("Can convert to a Type")
    public void convertToType() {
        assertEquals(
                new Type("abc"),
                new Classifier("abc").asType()
        );
    }
}
