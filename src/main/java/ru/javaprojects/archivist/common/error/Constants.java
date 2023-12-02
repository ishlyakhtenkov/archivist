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
    public static final String DUPLICATE_DOCUMENT_CHANGE_NOTICE_MESSAGE = "Document already has change by this change notice";
    public static final String DUPLICATE_DOCUMENT_CHANGE_NUMBER_MESSAGE = "Document already has this change number";
    public static final String DOCUMENT_HAS_REF_TO_COMPANY_MESSAGE = "Сannot delete the company because some documents reference it";
    public static final String DOCUMENT_HAS_REF_TO_DEPARTMENT_MESSAGE = "Сannot delete the department because some documents reference it";
    public static final String CHANGE_NOTICE_HAS_REF_TO_DEPARTMENT_MESSAGE = "Сannot delete the department because some change notices reference it";

    private static final Map<String, String> dbConstraintsMap = new HashMap<>();

    static {
        dbConstraintsMap.put("users_unique_email_idx", DUPLICATE_EMAIL_MESSAGE);
        dbConstraintsMap.put("companies_unique_name_idx", DUPLICATE_COMPANY_NAME_MESSAGE);
        dbConstraintsMap.put("departments_unique_name_idx", DUPLICATE_DEPARTMENT_NAME_MESSAGE);
        dbConstraintsMap.put("documents_unique_decimal_number_idx", DUPLICATE_DECIMAL_NUMBER_MESSAGE);
        dbConstraintsMap.put("documents_unique_inventory_number_idx", DUPLICATE_INVENTORY_NUMBER_MESSAGE);
        dbConstraintsMap.put("applicabilities_unique_document_applicability_idx", DUPLICATE_DOCUMENT_APPLICABILITY_MESSAGE);
        dbConstraintsMap.put("applicabilities_unique_primal_applicability_idx", DUPLICATE_PRIMAL_APPLICABILITY_MESSAGE);
        dbConstraintsMap.put("document_contents_unique_document_change_number_idx", DUPLICATE_CONTENT_CHANGE_NUMBER_MESSAGE);
        dbConstraintsMap.put("document_content_files_unique_document_content_name_idx", DUPLICATE_CONTENT_FILE_NAME_MESSAGE);
        dbConstraintsMap.put("sendings_unique_document_invoice_idx", DUPLICATE_SENDING_INVOICE_NUMBER_MESSAGE);
        dbConstraintsMap.put("changes_unique_document_change_notice_idx", DUPLICATE_DOCUMENT_CHANGE_NOTICE_MESSAGE);
        dbConstraintsMap.put("changes_unique_document_change_number_idx", DUPLICATE_DOCUMENT_CHANGE_NUMBER_MESSAGE);
        dbConstraintsMap.put("public.documents foreign key(original_holder_id) references public.companies(id)", DOCUMENT_HAS_REF_TO_COMPANY_MESSAGE);
        dbConstraintsMap.put("public.documents foreign key(developer_id) references public.departments(id)", DOCUMENT_HAS_REF_TO_DEPARTMENT_MESSAGE);
        dbConstraintsMap.put("public.change_notices foreign key(developer_id) references public.departments(id)", CHANGE_NOTICE_HAS_REF_TO_DEPARTMENT_MESSAGE);
    }

    public static Optional<String> getDbConstraintMessage(String exceptionMessage) {
        exceptionMessage = exceptionMessage.toLowerCase();
        for (Map.Entry<String, String> entry : dbConstraintsMap.entrySet()) {
            if (exceptionMessage.contains(entry.getKey())) {
                return Optional.of(entry.getValue());
            }
        }
        return Optional.empty();
    }
}
