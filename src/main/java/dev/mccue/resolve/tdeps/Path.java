package dev.mccue.resolve.tdeps;

import dev.mccue.resolve.util.LL;

public record Path(LL<Object> value) {
    boolean isEmpty() {
        return !(value instanceof LL.Nil<Object>);
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append("[");
        var root = value;
        boolean first = true;
        while (root instanceof LL.Cons<Object> cons) {
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
}

