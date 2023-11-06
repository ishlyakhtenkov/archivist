package ru.javaprojects.archivist.documents.to;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.javaprojects.archivist.documents.model.Status;

public class NotOriginalStatusValidator implements ConstraintValidator<NotOriginalStatus, Status> {
    @Override
    public boolean isValid(Status value, ConstraintValidatorContext context) {
        return value != Status.ORIGINAL;
    }
}
