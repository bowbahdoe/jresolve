package dev.mccue.resolve.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class ModuleNameTest {
    @Test
    @DisplayName("Module name doesn't transform value")
    public void emptyModuleNameHasEmptyValue() {
        assertEquals("abc", new ModuleName("abc").value());
        assertEquals("ABC", new ModuleName("ABC").value());
    }

    @Test
    @DisplayName("ModuleNames are lexicographically sortable")
    public void moduleNamesAreSortableByValue() {
        var moduleNames = new ArrayList<>(List.of(
                new ModuleName("f"),
                new ModuleName("e"),
                new ModuleName("a"),
                new ModuleName("b"),
                new ModuleName("d"),
                new ModuleName("c")
        ));

        Collections.sort(moduleNames);

        assertEquals(List.of(
                new ModuleName("a"),
                new ModuleName("b"),
                new ModuleName("c"),
                new ModuleName("d"),
                new ModuleName("e"),
                new ModuleName("f")
        ), moduleNames);
    }

    @Test
    @DisplayName("Cant make a ModuleName with a null value")
    public void cantMakeNullValuedModuleName() {
        assertThrows(
                NullPointerException.class,
                () -> new ModuleName(null),
                "Should not be able to make a ModuleName with a null value."
        );
    }


    @Test
    @DisplayName("Can use map to update the value in a ModuleName")
    public void mapType() {
        assertEquals(
                new ModuleName("ABC"),
                new ModuleName("abc").map(String::toUpperCase)
        );
    }

    @Test
    @DisplayName("The result of map cannot be null")
    public void nullMapType() {
        assertThrows(
                NullPointerException.class,
                () -> new ModuleName("").map(__ -> null),
                "Should not be able to return null from map."
        );
    }

}
