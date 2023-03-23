package dev.mccue.resolve.util;

import java.util.*;

/*
 * Basic Immutable Linked List Implementation.
 */
public sealed interface LL<T> extends Iterable<T> {
    record Nil<T>() implements LL<T> {
        @Override
        public Optional<T> headOption() {
            return Optional.empty();
        }
    }
    record Cons<T>(T head, LL<T> tail) implements LL<T> {
        public Cons {
            Objects.requireNonNull(head, "head must not be null");
            Objects.requireNonNull(tail, "tail must not be null");
        }

        @Override
        public Optional<T> headOption() {
            return Optional.of(head);
        }
    }

    default LL.Cons<T> prepend(T first) {
        return new LL.Cons<>(first, this);
    }

    Optional<T> headOption();

    static <T> LL<T> fromJavaList(List<T> list) {
        LL<T> head = new LL.Nil<>();
        for (int i = list.size() - 1; i >= 0; i--) {
            head = new LL.Cons<>(
                    list.get(i),
                    head
            );
        }
        return head;
    }

    default Cons<T> assumeNotEmpty() {
        if (!(this instanceof LL.Cons<T> cons)) {
            throw new IllegalStateException("Assumed to be not empty");
        }
        else {
            return cons;
        }
    }

    @Override
    default Iterator<T> iterator() {
        var self = this;
        return new Iterator<T>() {
            LL<T> head = self;
            @Override
            public boolean hasNext() {
                return head instanceof LL.Cons<T>;
            }

            @Override
            public T next() {
                if (head instanceof LL.Cons<T> cons) {
                    head = cons.tail;
                    return cons.head;
                }
                else {
                    throw new NoSuchElementException();
                }
            }
        };
    }
}
