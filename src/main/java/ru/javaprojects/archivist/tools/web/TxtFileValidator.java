package ru.javaprojects.archivist.tools.web;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

public class TxtFileValidator implements ConstraintValidator<TxtFile, MultipartFile> {
    @Override
    public boolean isValid(MultipartFile value, ConstraintValidatorContext ctx) {
        return value == null || Objects.requireNonNull(value.getOriginalFilename()).endsWith(".txt");
    }
}
