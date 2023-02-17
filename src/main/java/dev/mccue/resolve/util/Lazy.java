package dev.mccue.resolve.util;

import java.util.function.Supplier;

public final class Lazy<T> {
    private volatile Supplier<? extends T> supplier;
    private T value;

    public Lazy(Supplier<? extends T> supplier) {
        this.supplier = supplier;
        this.value = null;
    }

    public T get() {
        if (supplier == null) {
            return value;
        }
        else {
            var s = supplier;
            if (s != null) {
                this.value = supplier.get();
                supplier = null;
            }
            return this.value;
        }
    }
}
