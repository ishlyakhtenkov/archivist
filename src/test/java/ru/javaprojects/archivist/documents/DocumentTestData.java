package ru.javaprojects.archivist.documents;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.javaprojects.archivist.MatcherFactory;
import ru.javaprojects.archivist.documents.model.Applicability;
import ru.javaprojects.archivist.documents.model.Document;
import ru.javaprojects.archivist.documents.to.ApplicabilityTo;

import java.time.LocalDate;

import static java.time.Month.*;
import static ru.javaprojects.archivist.CommonTestData.*;
import static ru.javaprojects.archivist.companies.CompanyTestData.company2;
import static ru.javaprojects.archivist.companies.CompanyTestData.company3;
import static ru.javaprojects.archivist.departments.DepartmentTestData.department1;
import static ru.javaprojects.archivist.departments.DepartmentTestData.department2;
import static ru.javaprojects.archivist.documents.model.Letter.O;
import static ru.javaprojects.archivist.documents.model.Letter.O1;
import static ru.javaprojects.archivist.documents.model.Status.DUPLICATE;
import static ru.javaprojects.archivist.documents.model.Status.ORIGINAL;
import static ru.javaprojects.archivist.documents.model.Type.DIGITAL;
import static ru.javaprojects.archivist.documents.model.Type.PAPER;

public class DocumentTestData {
    public static final MatcherFactory.Matcher<Document> DOCUMENT_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(Document.class, "originalHolder", "developer");

    public static final long DOCUMENT1_ID = 100014;
    public static final long DOCUMENT3_ID = 100016;
    public static final long DOCUMENT5_ID = 100018;

    public static final String DOCUMENTS_ATTRIBUTE = "documents";
    public static final String DOCUMENT_ATTRIBUTE = "document";

    public static final String DECIMAL_NUMBER_PARAM = "decimalNumber";
    public static final String INVENTORY_NUMBER_PARAM = "inventoryNumber";
    public static final String ACCOUNTING_DATE_PARAM = "accountingDate";
    public static final String STATUS_PARAM = "status";
    public static final String LETTER_PARAM = "letter";
    public static final String TYPE_PARAM = "type";
    public static final String ANNULLED_PARAM = "annulled";
    public static final String COMMENT_PARAM = "comment";
    public static final String DEVELOPER_PARAM = "developer";
    public static final String ORIGINAL_HOLDER_PARAM = "originalHolder";

    public static final Document document1 = new Document(DOCUMENT1_ID, "Block M21", "VUIA.465521.004", "926531",
            LocalDate.of(2023, MARCH, 24), ORIGINAL, O1, DIGITAL, false, "some comment", department1, company3);

    public static final Document document3 = new Document(DOCUMENT3_ID, "Panel B45", "UPIA.421478.001-01", "456213",
            LocalDate.of(2021, MAY, 18), ORIGINAL, null, DIGITAL, false, null, department2, company2);


    public static Document getNew() {
        return new Document(null, "newName", "newDecimalNumber", "newInvNum", LocalDate.of(2023, OCTOBER, 12),
                ORIGINAL, O, PAPER, false, "newComment", department1, company3);
    }

    public static MultiValueMap<String, String> getNewParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        Document newDocument = getNew();
        params.add(NAME_PARAM, newDocument.getName());
        params.add(DECIMAL_NUMBER_PARAM, newDocument.getDecimalNumber());
        params.add(INVENTORY_NUMBER_PARAM, newDocument.getInventoryNumber());
        params.add(ACCOUNTING_DATE_PARAM, newDocument.getAccountingDate().toString());
        params.add(STATUS_PARAM, newDocument.getStatus().toString());
        params.add(LETTER_PARAM, newDocument.getLetter().toString());
        params.add(TYPE_PARAM, newDocument.getType().toString());
        params.add(ANNULLED_PARAM, FALSE);
        params.add(COMMENT_PARAM, newDocument.getComment());
        params.add(DEVELOPER_PARAM, String.valueOf(newDocument.getDeveloper().getId()));
        params.add(ORIGINAL_HOLDER_PARAM, String.valueOf(newDocument.getOriginalHolder().getId()));
        return params;
    }

    public static MultiValueMap<String, String> getNewInvalidParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(NAME_PARAM, "A");
        params.add(DECIMAL_NUMBER_PARAM, "");
        params.add(INVENTORY_NUMBER_PARAM, "");
        params.add(COMMENT_PARAM, "<p>dsfsdf<p>");
        return params;
    }

    public static Document getUpdated() {
        return new Document(DOCUMENT1_ID, "updatedName", "updatedDecimalNumber", "updInvNum", LocalDate.of(2021, JULY, 7),
                DUPLICATE, O1, DIGITAL, true, "updatedComment", department2, company2);
    }

    public static MultiValueMap<String, String> getUpdatedParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        Document updatedDocument = getUpdated();
        params.add(ID, String.valueOf(DOCUMENT1_ID));
        params.add(NAME_PARAM, updatedDocument.getName());
        params.add(DECIMAL_NUMBER_PARAM, updatedDocument.getDecimalNumber());
        params.add(INVENTORY_NUMBER_PARAM, updatedDocument.getInventoryNumber());
        params.add(ACCOUNTING_DATE_PARAM, updatedDocument.getAccountingDate().toString());
        params.add(STATUS_PARAM, updatedDocument.getStatus().toString());
        params.add(LETTER_PARAM, updatedDocument.getLetter().toString());
        params.add(TYPE_PARAM, updatedDocument.getType().toString());
        params.add(ANNULLED_PARAM, TRUE);
        params.add(COMMENT_PARAM, updatedDocument.getComment());
        params.add(DEVELOPER_PARAM, String.valueOf(updatedDocument.getDeveloper().getId()));
        params.add(ORIGINAL_HOLDER_PARAM, String.valueOf(updatedDocument.getOriginalHolder().getId()));
        return params;
    }

    public static MultiValueMap<String, String> getUpdatedInvalidParams() {
        MultiValueMap<String, String> params = getNewInvalidParams();
        params.add(ID, String.valueOf(DOCUMENT1_ID));
        return params;
    }

    public static final long DOCUMENT_5_APPLICABILITY_1_ID = 100020;
    public static final long DOCUMENT_5_APPLICABILITY_2_ID = 100021;

    public static final MatcherFactory.Matcher<Applicability> APPLICABILITY_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(Applicability.class, "document", "applicability");

    public static final Applicability applicability1 = new Applicability(DOCUMENT_5_APPLICABILITY_1_ID, null, document1, true);
    public static final Applicability applicability2 = new Applicability(DOCUMENT_5_APPLICABILITY_2_ID, null, document3, false);

    public static ApplicabilityTo getNewApplicabilityTo() {
        return new ApplicabilityTo(null, DOCUMENT3_ID, document1.getDecimalNumber(), false);
    }
}
