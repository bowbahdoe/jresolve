package dev.mccue.resolve.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public final class TypeTest {
    @Test
    @DisplayName("Types are lexicographically sortable")
    public void typeIsSortableByValue() {
        var types = new ArrayList<>(List.of(
                new Type("f"),
                new Type("e"),
                new Type("a"),
                new Type("b"),
                new Type("d"),
                new Type("c")
        ));

        Collections.sort(types);

        assertEquals(List.of(
                new Type("a"),
                new Type("b"),
                new Type("c"),
                new Type("d"),
                new Type("e"),
                new Type("f")
        ),types);
    }

    @Test
    @DisplayName("The 'empty' type is reported as empty by isEmpty")
    public void emptyTypeIsEmpty() {
        assertTrue(
                Type.EMPTY.isEmpty(),
                "empty type is empty"
        );
    }

    @Test
    @DisplayName("The 'empty' type holds an empty string")
    public void emptyTypeHoldsEmptyString() {
        assertEquals(
                Type.EMPTY.value(),
                ""
        );
    }

    @Test
    @DisplayName("Cant make a Type with a null value")
    public void cantMakeNullValuedType() {
        assertThrows(
                NullPointerException.class,
                () -> new Type(null),
                "Should not be able to make a Type with a null value."
        );
    }

    @Test
    @DisplayName("Can use map to update the value in a Type")
    public void mapType() {
        assertEquals(
                new Type("ABC"),
                new Type("abc").map(String::toUpperCase)
        );
    }

    @Test
    @DisplayName("The result of map cannot be null")
    public void nullMapType() {
        assertThrows(
                NullPointerException.class,
                () -> new Type("").map(__ -> null),
                "Should not be able to return null from map."
        );
    }

    @Test
    @DisplayName("Can convert to an Extension")
    public void convertToExtension() {
        assertEquals(
                new Extension("abc"),
                new Type("abc").asExtension()
        );
    }
}
