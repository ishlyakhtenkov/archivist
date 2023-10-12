package ru.javaprojects.archivist.documents.web;

import lombok.AllArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import ru.javaprojects.archivist.documents.DocumentRepository;
import ru.javaprojects.archivist.documents.model.Document;

import java.util.Objects;

import static ru.javaprojects.archivist.common.util.validation.Constants.DUPLICATE_ERROR_CODE;

@Component
@AllArgsConstructor
public class UniqueDocumentInventoryNumberValidator implements org.springframework.validation.Validator {
    public static final String DUPLICATE_INVENTORY_NUMBER_MESSAGE = "Document with this inventory number already exists";

    private final DocumentRepository repository;

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return Document.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        Document document = ((Document) target);
        if (StringUtils.hasText(document.getInventoryNumber())) {
            repository.findByInventoryNumberIgnoreCase(document.getInventoryNumber())
                    .ifPresent(dbDocument -> {
                        if (document.isNew() || !Objects.equals(document.getId(), dbDocument.getId())) {
                            errors.rejectValue("inventoryNumber", DUPLICATE_ERROR_CODE, DUPLICATE_INVENTORY_NUMBER_MESSAGE);
                        }
                    });
        }
    }
}
