package dev.mccue.resolve.core;

import dev.mccue.resolve.doc.Coursier;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Coursier(
        value = "https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/core/Definitions.scala#L28-L79",
        details = "Did not translate the lazy hash code or instance caching."
)
public record Module(
        Organization organization,
        ModuleName name,
        Map<String, String> attributes
) {
    public Module(
            Organization organization,
            ModuleName name,
            Map<String, String> attributes
    ) {
        this.organization = Objects.requireNonNull(
                organization,
                "organization must not be null"
        );
        this.name = Objects.requireNonNull(
                name,
                "name must not be null"
        );
        this.attributes = Map.copyOf(Objects.requireNonNull(
                attributes,
                "attributes must not be null"
        ));
    }

    public Module trim() {
        return new Module(
                this.organization.map(String::trim),
                this.name.map(String::trim),
                this.attributes
        );
    }

    public String attributesStr() {
        return attributes.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining(";"));
    }

    public String nameWithAttributes() {
        return name.value() + (attributes.isEmpty() ? "" : (";" + attributesStr()));
    }

    public String repr() {
        return organization.value() + ":" + nameWithAttributes();
    }

    @Override
    public String toString() {
        return repr();
    }

    public String orgName() {
        return organization.value() + ":" + name.value();
    }
}
