package dev.mccue.resolve.util;

import org.junit.jupiter.api.Test;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public final class Tuple4Test {
    @Test
    public void exerciseTuple4() {
        var t1 = new Tuple4<>(null, null, null, null);
        assertNull(t1.first());
        assertNull(t1.second());
        assertNull(t1.third());
        assertNull(t1.fourth());



        var t2 = new Tuple4<>("a", 1, new Tuple2<>(1, 2), URI.create("some.com/b/c"));
        assertEquals("a", t2.first());
        assertEquals(1, t2.second());

        assertEquals(new Tuple2<>(1, 2), t2.third());
        assertEquals(URI.create("some.com/b/c"), t2.fourth());
        assertEquals(
                new Tuple4<>("a", 1, new Tuple2<>(1, 2), URI.create("some.com/b/c")),
                new Tuple4<>("a", 1, new Tuple2<>(1, 2), URI.create("some.com/b/c"))
        );

        assertEquals(
                new Tuple4<>("a", 1, new Tuple2<>(1, 2), URI.create("some.com/b/c")).hashCode(),
                new Tuple4<>("a", 1, new Tuple2<>(1, 2), URI.create("some.com/b/c")).hashCode()
        );
    }
}
