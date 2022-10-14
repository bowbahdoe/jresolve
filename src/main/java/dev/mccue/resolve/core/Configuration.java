package dev.mccue.resolve.core;

import dev.mccue.resolve.doc.Coursier;
import dev.mccue.resolve.doc.StackOverflow;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Coursier("https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/core/Definitions.scala#L165-L197")
public record Configuration(String value) implements Comparable<Configuration> {
    public static final Configuration EMPTY = new Configuration("");
    public static final Configuration COMPILE = new Configuration("compile");
    public static final Configuration RUNTIME = new Configuration("runtime");
    public static final Configuration TEST = new Configuration("test");
    public static final Configuration DEFAULT = new Configuration("default");
    public static final Configuration DEFAULT_COMPILE = new Configuration("default(compile)");
    public static final Configuration PROVIDED = new Configuration("provided");
    public static final Configuration IMPORT = new Configuration("import");
    public static final Configuration OPTIONAL = new Configuration("optional");
    public static final Configuration ALL = new Configuration("*");

    public Configuration {
        Objects.requireNonNull(value, "value must not be null");
    }

    public boolean isEmpty() {
        return this.value.isEmpty();
    }

    public Configuration map(Function<String, String> f) {
        return new Configuration(f.apply(this.value));
    }

    @StackOverflow(
            value = "https://stackoverflow.com/a/653416",
            details = """
            In coursier this method is called --> and it makes the ->
            like in Ivy. On StackOverflow I saw that the thing on the
            right- hand side of the arrow in Ivy
            is called the 'mapped child element'.
            """)
    public Configuration withMappedChildElement(Configuration target) {
        return new Configuration(this.value + "->" + target.value);
    }

    @Override
    public int compareTo(Configuration o) {
        return this.value.compareTo(o.value);
    }

    public static Configuration join(List<Configuration> configurations) {
        return new Configuration(
                configurations.stream()
                        .map(Configuration::value)
                        .collect(Collectors.joining(";"))
        );
    }
}
