package dev.mccue.resolve;

import dev.mccue.resolve.util.LL;

import java.util.Iterator;

public record Path(LL<Library> value) implements Iterable<Library> {
    boolean isEmpty() {
        return !(value instanceof LL.Nil<Library>);
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append("[");
        var root = value;
        boolean first = true;
        while (root instanceof LL.Cons<Library> cons) {
            if (!first) {
                sb.append(", ");
            }
            first = false;

            sb.append(cons.head());
            root = cons.tail();
        }
        sb.append("]");
        return sb.toString();
    }


    @Override
    public Iterator<Library> iterator() {
        return value.iterator();
    }
}

