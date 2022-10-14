package dev.mccue.resolve.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public final class Tuple2Test {
    @Test
    public void exerciseTuple2() {
        var t1 = new Tuple2<>(null, null);
        assertNull(t1.first());
        assertNull(t1.second());

        var t2 = new Tuple2<>("a", 1);
        assertEquals("a", t2.first());
        assertEquals(1, t2.second());

        assertEquals(
                new Tuple2<>(123, "abc"),
                new Tuple2<>(123, "abc")
        );

        assertEquals(
                new Tuple2<>(123, "abc").hashCode(),
                new Tuple2<>(123, "abc").hashCode()
        );
    }
}
