package dev.mccue.resolve.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public final class OrganizationTest {
    @Test
    @DisplayName("Organization doesn't transform value")
    public void emptyModuleNameHasEmptyValue() {
        assertEquals("abc", new Organization("abc").value());
        assertEquals("ABC", new Organization("ABC").value());
    }

    @Test
    @DisplayName("Organizations are lexicographically sortable")
    public void organizationsAreSortableByValue() {
        var organizations = new ArrayList<>(List.of(
                new Organization("f"),
                new Organization("e"),
                new Organization("a"),
                new Organization("b"),
                new Organization("d"),
                new Organization("c")
        ));

        Collections.sort(organizations);

        assertEquals(List.of(
                new Organization("a"),
                new Organization("b"),
                new Organization("c"),
                new Organization("d"),
                new Organization("e"),
                new Organization("f")
        ), organizations);
    }

    @Test
    @DisplayName("Cant make a Organization with a null value")
    public void cantMakeNullValuedModuleName() {
        assertThrows(
                NullPointerException.class,
                () -> new Organization(null),
                "Should not be able to make a Organization with a null value."
        );
    }


    @Test
    @DisplayName("Can use map to update the value in a Organization")
    public void mapType() {
        assertEquals(
                new Organization("ABC"),
                new Organization("abc").map(String::toUpperCase)
        );
    }

    @Test
    @DisplayName("The result of map cannot be null")
    public void nullMapType() {
        assertThrows(
                NullPointerException.class,
                () -> new Organization("").map(__ -> null),
                "Should not be able to return null from map."
        );
    }

}
