package dev.mccue.resolve.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public final class ExtensionTest {
    @Test
    @DisplayName("Extensions are lexicographically sortable")
    public void extensionIsSortableByValue() {
        var extensions = new ArrayList<>(List.of(
                new Extension("f"),
                new Extension("e"),
                new Extension("a"),
                new Extension("b"),
                new Extension("d"),
                new Extension("c")
        ));

        Collections.sort(extensions);

        assertEquals(List.of(
                new Extension("a"),
                new Extension("b"),
                new Extension("c"),
                new Extension("d"),
                new Extension("e"),
                new Extension("f")
        ),extensions);
    }

    @Test
    @DisplayName("The 'empty' extension is reported as empty by isEmpty")
    public void emptyExtensionIsEmpty() {
        assertTrue(
                Extension.EMPTY.isEmpty(),
                "empty extension is empty"
        );
    }

    @Test
    @DisplayName("The 'empty' extension holds an empty string")
    public void emptyExtensionHoldsEmptyString() {
        assertEquals(
                Extension.EMPTY.value(),
                ""
        );
    }

    @Test
    @DisplayName("Cant make a Extension with a null value")
    public void cantMakeNullValuedExtension() {
        assertThrows(
                NullPointerException.class,
                () -> new Extension(null),
                "Should not be able to make a Extension with a null value."
        );
    }

    @Test
    @DisplayName("Can use map to update the value in a Extension")
    public void mapExtension() {
        assertEquals(
                new Extension("ABC"),
                new Extension("abc").map(String::toUpperCase)
        );
    }

    @Test
    @DisplayName("The result of map cannot be null")
    public void nullMapExtension() {
        assertThrows(
                NullPointerException.class,
                () -> new Extension("").map(__ -> null),
                "Should not be able to return null from map."
        );
    }

    @Test
    @DisplayName("Can convert to a Type")
    public void convertToType() {
        assertEquals(
                new Type("abc"),
                new Extension("abc").asType()
        );
    }
}
