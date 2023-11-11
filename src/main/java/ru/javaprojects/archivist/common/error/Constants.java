package ru.javaprojects.archivist.common.error;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Constants {
    public static final String DUPLICATE_ERROR_CODE = "Duplicate";

    public static final String DUPLICATE_EMAIL_MESSAGE = "User with this email already exists";
    public static final String DUPLICATE_COMPANY_NAME_MESSAGE = "Company with this name already exists";
    public static final String DUPLICATE_DEPARTMENT_NAME_MESSAGE = "Department with this name already exists";
    public static final String DUPLICATE_DECIMAL_NUMBER_MESSAGE = "Document with this decimal number already exists";
    public static final String DUPLICATE_INVENTORY_NUMBER_MESSAGE = "Document with this inventory number already exists";
    public static final String DUPLICATE_DOCUMENT_APPLICABILITY_MESSAGE = "Document already has this applicability";
    public static final String DUPLICATE_PRIMAL_APPLICABILITY_MESSAGE = "Document already has primal applicability";
    public static final String DUPLICATE_CONTENT_CHANGE_NUMBER_MESSAGE = "Document already has content with this change number";
    public static final String DUPLICATE_CONTENT_FILE_NAME_MESSAGE = "Document content already has file with this name";
    public static final String DUPLICATE_SENDING_INVOICE_NUMBER_MESSAGE = "Document already has sanding with this invoice";

    private static final Map<String, String> dbUniqueConstraintsMap = new HashMap<>();

    static {
        dbUniqueConstraintsMap.put("users_unique_email_idx", DUPLICATE_EMAIL_MESSAGE);
        dbUniqueConstraintsMap.put("companies_unique_name_idx", DUPLICATE_COMPANY_NAME_MESSAGE);
        dbUniqueConstraintsMap.put("departments_unique_name_idx", DUPLICATE_DEPARTMENT_NAME_MESSAGE);
        dbUniqueConstraintsMap.put("documents_unique_decimal_number_idx", DUPLICATE_DECIMAL_NUMBER_MESSAGE);
        dbUniqueConstraintsMap.put("documents_unique_inventory_number_idx", DUPLICATE_INVENTORY_NUMBER_MESSAGE);
        dbUniqueConstraintsMap.put("applicabilities_unique_document_applicability_idx", DUPLICATE_DOCUMENT_APPLICABILITY_MESSAGE);
        dbUniqueConstraintsMap.put("applicabilities_unique_primal_applicability_idx", DUPLICATE_PRIMAL_APPLICABILITY_MESSAGE);
        dbUniqueConstraintsMap.put("document_contents_unique_document_change_number_idx", DUPLICATE_CONTENT_CHANGE_NUMBER_MESSAGE);
        dbUniqueConstraintsMap.put("document_content_files_unique_document_content_name_idx", DUPLICATE_CONTENT_FILE_NAME_MESSAGE);
        dbUniqueConstraintsMap.put("sendings_unique_document_invoice_idx", DUPLICATE_SENDING_INVOICE_NUMBER_MESSAGE);
    }

    public static Optional<String> getDbConstraintMessage(String exceptionMessage) {
        exceptionMessage = exceptionMessage.toLowerCase();
        for (Map.Entry<String, String> entry : dbUniqueConstraintsMap.entrySet()) {
            if (exceptionMessage.contains(entry.getKey())) {
                return Optional.of(entry.getValue());
            }
        }
        return Optional.empty();
    }
}
