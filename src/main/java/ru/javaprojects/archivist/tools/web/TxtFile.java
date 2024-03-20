package ru.javaprojects.archivist.tools.web;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = TxtFileValidator.class)
@Target({METHOD, FIELD, ANNOTATION_TYPE, PARAMETER})
@Retention(RUNTIME)
public @interface TxtFile {
    String message() default "must be txt";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
