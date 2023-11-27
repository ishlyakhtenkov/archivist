package ru.javaprojects.archivist.changenotices.web;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;

import java.util.Objects;

public class PdfFileValidator implements ConstraintValidator<PdfFile, MultipartFile> {
    @Override
    public boolean isValid(MultipartFile value, ConstraintValidatorContext ctx) {
        return value == null || Objects.requireNonNull(value.getOriginalFilename()).endsWith(".pdf");
    }
}
