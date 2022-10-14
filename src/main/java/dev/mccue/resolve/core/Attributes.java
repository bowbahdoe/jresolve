package dev.mccue.resolve.core;

import dev.mccue.resolve.doc.Coursier;

import java.util.Objects;

@Coursier("https://github.com/coursier/coursier/blob/f5f0870/modules/core/shared/src/main/scala/coursier/core/Definitions.scala#L199-L226")
public record Attributes(
        Type type,
        Classifier classifier
) {
    public static final Attributes EMPTY = new Attributes(Type.EMPTY, Classifier.EMPTY);

    public Attributes {
        Objects.requireNonNull(type, "type must not be null");
        Objects.requireNonNull(classifier, "classifier must not be null");
    }

    public Type packaging() {
        if (this.type.isEmpty()) {
            return Type.JAR;
        }
        else {
            return this.type;
        }
    }

    public String packagingAndClassifier() {
        if (isEmpty()) {
            return "";
        }
        else if (classifier.isEmpty()) {
            return packaging().value();
        }
        else {
            return packaging().value() + ":" + classifier.value();
        }
    }

    public Publication publication(String name, Extension extension) {
        return new Publication(
                name,
                this.type,
                extension,
                this.classifier
        );
    }

    public boolean isEmpty() {
        return this.type.isEmpty() && this.classifier.isEmpty();
    }
}
