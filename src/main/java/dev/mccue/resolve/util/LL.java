package dev.mccue.resolve.util;

import java.util.*;

/*
 * Basic Immutable Linked List Implementation.
 */
public sealed interface LL<T> extends Iterable<T> {
    record Nil<T>() implements LL<T> {
        @Override
        public LL.Nil<T> reverse() {
            return this;
        }

        @Override
        public Optional<T> headOption() {
            return Optional.empty();
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public String toString() {
            return "[]";
        }
    }
    record Cons<T>(T head, LL<T> tail) implements LL<T> {
        public Cons {
            Objects.requireNonNull(head, "head must not be null");
            Objects.requireNonNull(tail, "tail must not be null");
        }

        @Override
        public LL.Cons<T> reverse() {
            var top = this.tail;
            LL.Cons<T> reversed = new LL.Nil<T>().prepend(head);
            while (top instanceof LL.Cons<T> hasHead) {
                reversed = reversed.prepend(hasHead.head);
                top = hasHead.tail;
            }
            return reversed;
        }

        @Override
        public Optional<T> headOption() {
            return Optional.of(head);
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public String toString() {
            var sb = new StringBuilder();
            sb.append("[");
            LL<T> self = this;
            while (self instanceof LL.Cons<T> hasHead) {
                sb.append(hasHead.head);
                if (hasHead.tail instanceof LL.Cons<T>) {
                    sb.append(", ");
                }
                self = hasHead.tail;
            }
            sb.append("]");
            return sb.toString();
        }
    }

    default LL.Cons<T> prepend(T first) {
        return new LL.Cons<>(first, this);
    }

    LL<T> reverse();

    default LL.Cons<T> append(T last) {
        return this.reverse().prepend(last).reverse();
    }

    Optional<T> headOption();

    boolean isEmpty();

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
