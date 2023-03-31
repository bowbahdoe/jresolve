package dev.mccue.resolve.doc;

public @interface Maven {
    String value();

    String details() default "";
}
