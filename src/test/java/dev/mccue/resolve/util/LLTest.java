package dev.mccue.resolve.util;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public final class LLTest {
    @Test
    public void testFromJavaList() {
        var ll1 = LL.fromJavaList(List.of());
        assertEquals(ll1, new LL.Nil<>());

        var ll2 = LL.fromJavaList(List.of(1));
        assertEquals(ll2, new LL.Cons<>(1, new LL.Nil<>()));

        var ll3 = LL.fromJavaList(List.of(1, 2, 3, 4, 5));

        assertEquals(ll3, new LL.Cons<>(
                1,
                new LL.Cons<>(
                        2,
                        new LL.Cons<>(
                                3,
                                new LL.Cons<>(4,
                                        new LL.Cons<>(
                                                5,
                                                new LL.Nil<>()
                                        )
                                )
                        )
                )
        ));
    }

    @Test
    public void testAssumeNotEmpty() {
        LL<?> ll1 = new LL.Nil<>();
        assertThrows(
                IllegalStateException.class,
                ll1::assumeNotEmpty
        );
        LL<?> ll2 = new LL.Cons<>(1, new LL.Nil<>());
        assertEquals(
                ll2,
                ll2.assumeNotEmpty()
        );
        assertEquals(
                1,
                ll2.assumeNotEmpty().head()
        );
    }

    @Test
    public void testHeadOption() {
        LL<?> ll1 = new LL.Nil<>();
        assertEquals(
                Optional.empty(),
                ll1.headOption()
        );
        LL<?> ll2 = new LL.Cons<>(1, new LL.Nil<>());
        assertEquals(
                Optional.of(1),
                ll2.headOption()
        );
    }

    @Test
    public void assertThatItIsASealedInterfaceWithTwoRecordCases() {
        assertTrue(
                LL.class.isSealed()
        );

        assertTrue(
                LL.class.isInterface()
        );

        assertEquals(
                Set.of(LL.Cons.class, LL.Nil.class),
                Set.copyOf(Arrays.asList(LL.class.getPermittedSubclasses()))
        );

        assertTrue(
                LL.Nil.class.isRecord()
        );

        assertEquals(
                0,
                LL.Nil.class.getRecordComponents().length
        );

        assertTrue(
                LL.Cons.class.isRecord()
        );

        assertEquals(
                2,
                LL.Cons.class.getRecordComponents().length
        );

        assertEquals(
                "head",
                LL.Cons.class.getRecordComponents()[0].getName()
        );

        assertEquals(
                "tail",
                LL.Cons.class.getRecordComponents()[1].getName()
        );
    }

    @Test
    public void testIsPrefix() {
        assertTrue(
                LL.fromJavaList(List.of(1, 2, 3)).isPrefix(LL.fromJavaList(List.of(1, 2, 3, 4)))
        );

        assertFalse(
                LL.fromJavaList(List.of(1, 2, 3)).isPrefix(LL.fromJavaList(List.of(1, 2)))
        );

        assertTrue(
                LL.fromJavaList(List.of(1, 2, 3)).isPrefix(LL.fromJavaList(List.of(1, 2, 3)))
        );

        assertTrue(
                LL.fromJavaList(List.of()).isPrefix(LL.fromJavaList(List.of(1, 2, 3)))
        );

        assertTrue(
                LL.fromJavaList(List.of(1)).isPrefix(LL.fromJavaList(List.of(1, 2, 3)))
        );

        assertFalse(
                LL.fromJavaList(List.of(1)).isPrefix(LL.fromJavaList(List.of()))
        );
    }

    @Test
    public void appendTest() {
        assertEquals(
                LL.fromJavaList(List.of(1)),
                LL.fromJavaList(List.of()).append(1)
        );

        assertEquals(
                LL.fromJavaList(List.of(1, 2)),
                LL.fromJavaList(List.of()).append(1).append(2)
        );

        assertEquals(
                LL.fromJavaList(List.of(1, 2, 3)),
                LL.fromJavaList(List.of()).append(1).append(2).append(3)
        );
    }
}
