package dev.mccue.resolve.util;

import dev.mccue.resolve.doc.Clojure;
import dev.mccue.resolve.doc.Vavr;

import java.util.function.Supplier;

@Vavr("https://github.com/vavr-io/vavr/blob/f37e9ef/src/main/java/io/vavr/Lazy.java")
@Clojure("https://github.com/clojure/clojure/blob/d56812c/src/jvm/clojure/lang/Delay.java")
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
