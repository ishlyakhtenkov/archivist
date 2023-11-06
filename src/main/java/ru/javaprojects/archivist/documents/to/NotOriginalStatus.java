package ru.javaprojects.archivist.documents.to;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = NotOriginalStatusValidator.class)
@Target({METHOD, FIELD, ANNOTATION_TYPE})
@Retention(RUNTIME)
public @interface NotOriginalStatus {
    String message() default "must not be original";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
