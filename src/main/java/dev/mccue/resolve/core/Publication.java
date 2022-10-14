package dev.mccue.resolve.core;

import dev.mccue.resolve.doc.Coursier;

import java.util.Objects;

@Coursier(
        value = "https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/core/Definitions.scala#L379-L409",
        details = "Skipped implementing the instance caching present in the original."
)
public record Publication(
        String name,
        Type type,
        Extension extension,
        Classifier classifier
) {
    public static final Publication EMPTY = new Publication(
            "", Type.EMPTY, Extension.EMPTY, Classifier.EMPTY
    );

    public Publication {
        Objects.requireNonNull(name, "name must not be null");
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(extension, "extension must not be null");
        Objects.requireNonNull(classifier, "classifier must not be null");
    }

    public Attributes attributes() {
        return new Attributes(type, classifier);
    }

    public boolean isEmpty() {
        return this.name.isEmpty()
                && this.type.isEmpty()
                && this.extension.isEmpty()
                && this.classifier.isEmpty();
    }
}
