package dev.mccue.resolve;

import dev.mccue.resolve.doc.Coursier;
import dev.mccue.resolve.doc.StackOverflow;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Coursier("https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/core/Definitions.scala#L165-L197")
public record Scope(String value) implements Comparable<Scope> {
    public static final Scope EMPTY = new Scope("");
    public static final Scope COMPILE = new Scope("compile");
    public static final Scope RUNTIME = new Scope("runtime");
    public static final Scope TEST = new Scope("test");
    public static final Scope DEFAULT = new Scope("default");
    public static final Scope DEFAULT_COMPILE = new Scope("default(compile)");
    public static final Scope PROVIDED = new Scope("provided");
    public static final Scope IMPORT = new Scope("import");
    public static final Scope OPTIONAL = new Scope("optional");
    public static final Scope ALL = new Scope("*");

    public Scope {
        Objects.requireNonNull(value, "value must not be null");
    }

    public Scope map(Function<String, String> f) {
        return new Scope(f.apply(this.value));
    }

    @StackOverflow(
            value = "https://stackoverflow.com/a/653416",
            details = """
                In coursier this method is called --> and it makes the ->
                like in Ivy. On StackOverflow I saw that the thing on the
                right- hand side of the arrow in Ivy
                is called the 'mapped child element'.
                """)
    public Scope withMappedChildElement(Scope target) {
        return new Scope(this.value + "->" + target.value);
    }

    @Override
    public int compareTo(Scope o) {
        return this.value.compareTo(o.value);
    }

    public static Scope join(List<Scope> scopes) {
        return new Scope(
                scopes.stream()
                        .map(Scope::value)
                        .collect(Collectors.joining(";"))
        );
    }

    @Override
    public String toString() {
        if (this.equals(Scope.EMPTY)) {
            return "Scope[EMPTY]";
        }
        else {
            return "Scope[" +
                    "value='" + value + '\'' +
                    ']';
        }
    }
}