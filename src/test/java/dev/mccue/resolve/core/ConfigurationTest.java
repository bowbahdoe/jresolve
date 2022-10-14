package dev.mccue.resolve.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public final class ConfigurationTest {
    @Test
    @DisplayName("Configurations are lexicographically sortable")
    public void configurationIsSortableByValue() {
        var configurations = new ArrayList<>(List.of(
                new Configuration("f"),
                new Configuration("e"),
                new Configuration("a"),
                new Configuration("b"),
                new Configuration("d"),
                new Configuration("c")
        ));

        Collections.sort(configurations);

        assertEquals(List.of(
                new Configuration("a"),
                new Configuration("b"),
                new Configuration("c"),
                new Configuration("d"),
                new Configuration("e"),
                new Configuration("f")
        ), configurations);
    }

    @Test
    @DisplayName("The 'empty' configuration is reported as empty by isEmpty")
    public void emptyConfigurationIsEmpty() {
        assertTrue(
                Configuration.EMPTY.isEmpty(),
                "empty configuration is empty"
        );
    }

    @Test
    @DisplayName("The 'empty' configuration holds an empty string")
    public void emptyConfigurationHoldsEmptyString() {
        assertEquals(
                Configuration.EMPTY.value(),
                ""
        );
    }

    @Test
    @DisplayName("Cant make a Configuration with a null value")
    public void cantMakeNullValuedConfiguration() {
        assertThrows(
                NullPointerException.class,
                () -> new Configuration(null),
                "Should not be able to make a Configuration with a null value."
        );
    }

    @Test
    @DisplayName("Can use map to update the value in a Configuration")
    public void mapConfiguration() {
        assertEquals(
                new Configuration("ABC"),
                new Configuration("abc").map(String::toUpperCase)
        );
    }

    @Test
    @DisplayName("The result of map cannot be null")
    public void nullMapConfiguration() {
        assertThrows(
                NullPointerException.class,
                () -> new Configuration("").map(__ -> null),
                "Should not be able to return null from map."
        );
    }

    @Test
    @DisplayName("withMappedChildElement will join values with ->")
    public void testWithMappedChildElement() {
        assertEquals(
                new Configuration("a->b"),
                new Configuration("a")
                        .withMappedChildElement(new Configuration("b"))
        );

        assertEquals(
                new Configuration("a->b->c"),
                new Configuration("a")
                        .withMappedChildElement(
                                new Configuration("b")
                                        .withMappedChildElement(
                                                new Configuration("c")))
        );

        assertEquals(
                new Configuration("->->"),
                Configuration.EMPTY
                        .withMappedChildElement(Configuration.EMPTY)
                        .withMappedChildElement(Configuration.EMPTY)
        );
    }

    @Test
    @DisplayName("join will join configurations with semicolons")
    public void joinTest() {
        assertEquals(Configuration.EMPTY, Configuration.join(List.of()));
        assertEquals(
                new Configuration("a;b"),
                Configuration.join(List.of(
                        new Configuration("a"),
                        new Configuration("b")
                ))
        );
        assertEquals(
                new Configuration("a;b->c;d"),
                Configuration.join(List.of(
                        new Configuration("a"),
                        new Configuration("b").withMappedChildElement(
                                new Configuration("c")
                        ),
                        new Configuration("d")
                ))
        );
    }

}
