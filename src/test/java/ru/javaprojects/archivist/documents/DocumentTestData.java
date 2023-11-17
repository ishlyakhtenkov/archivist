package ru.javaprojects.archivist.documents;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.javaprojects.archivist.MatcherFactory;
import ru.javaprojects.archivist.documents.model.*;
import ru.javaprojects.archivist.documents.to.ApplicabilityTo;
import ru.javaprojects.archivist.documents.to.SendingTo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static java.time.Month.*;
import static ru.javaprojects.archivist.CommonTestData.*;
import static ru.javaprojects.archivist.companies.CompanyTestData.*;
import static ru.javaprojects.archivist.departments.DepartmentTestData.department1;
import static ru.javaprojects.archivist.departments.DepartmentTestData.department2;
import static ru.javaprojects.archivist.documents.model.Status.*;
import static ru.javaprojects.archivist.documents.model.Symbol.O;
import static ru.javaprojects.archivist.documents.model.Symbol.O1;
import static ru.javaprojects.archivist.documents.model.Type.DIGITAL;
import static ru.javaprojects.archivist.documents.model.Type.PAPER;

public class DocumentTestData {
    public static final MatcherFactory.Matcher<Document> DOCUMENT_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(Document.class, "originalHolder", "developer");

    public static final long DOCUMENT1_ID = 100014;
    public static final long DOCUMENT3_ID = 100016;
    public static final long DOCUMENT5_ID = 100018;
    public static final long DOCUMENT6_ID = 100019;

    public static final String NOT_EXISTED_DECIMAL_NUMBER = "VUIA.111111.222";

    public static final String DOCUMENTS_ATTRIBUTE = "documents";
    public static final String DOCUMENT_ATTRIBUTE = "document";

    public static final String DECIMAL_NUMBER_PARAM = "decimalNumber";
    public static final String INVENTORY_NUMBER_PARAM = "inventoryNumber";
    public static final String ACCOUNTING_DATE_PARAM = "accountingDate";
    public static final String STATUS_PARAM = "status";
    public static final String SYMBOL_PARAM = "symbol";
    public static final String TYPE_PARAM = "type";
    public static final String ANNULLED_PARAM = "annulled";
    public static final String SECRET_PARAM = "secret";
    public static final String COMMENT_PARAM = "comment";
    public static final String DEVELOPER_PARAM = "developer";
    public static final String ORIGINAL_HOLDER_PARAM = "originalHolder";
    public static final String FILE_LINK_PARAM = "fileLink";
    public static final String CHANGE_NUMBER_PARAM = "changeNumber";
    public static final String FILES_PARAM = "files";

    public static final Document document1 = new Document(DOCUMENT1_ID, "Block M21", "VUIA.465521.004", "926531",
            LocalDate.of(2023, MARCH, 24), ORIGINAL, O1, DIGITAL, false, false, "some comment", department1, company3);

    public static final Document document3 = new Document(DOCUMENT3_ID, "Panel B45", "UPIA.421478.001-01", "456213",
            LocalDate.of(2021, MAY, 18), ORIGINAL, null, DIGITAL, false, true, null, department2, company2);


    public static Document getNew() {
        return new Document(null, "newName", "newDecimalNumber", "newInvNum", LocalDate.of(2023, OCTOBER, 12),
                ORIGINAL, O, PAPER, false, false, "newComment", department1, company3);
    }

    public static MultiValueMap<String, String> getNewParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        Document newDocument = getNew();
        params.add(NAME_PARAM, newDocument.getName());
        params.add(DECIMAL_NUMBER_PARAM, newDocument.getDecimalNumber());
        params.add(INVENTORY_NUMBER_PARAM, newDocument.getInventoryNumber());
        params.add(ACCOUNTING_DATE_PARAM, newDocument.getAccountingDate().toString());
        params.add(STATUS_PARAM, newDocument.getStatus().name());
        params.add(SYMBOL_PARAM, newDocument.getSymbol().name());
        params.add(TYPE_PARAM, newDocument.getType().name());
        params.add(ANNULLED_PARAM, FALSE);
        params.add(SECRET_PARAM, FALSE);
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
                DUPLICATE, O1, DIGITAL, true, false, "updatedComment", department2, company2);
    }

    public static MultiValueMap<String, String> getUpdatedParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        Document updatedDocument = getUpdated();
        params.add(ID, String.valueOf(DOCUMENT1_ID));
        params.add(NAME_PARAM, updatedDocument.getName());
        params.add(DECIMAL_NUMBER_PARAM, updatedDocument.getDecimalNumber());
        params.add(INVENTORY_NUMBER_PARAM, updatedDocument.getInventoryNumber());
        params.add(ACCOUNTING_DATE_PARAM, updatedDocument.getAccountingDate().toString());
        params.add(STATUS_PARAM, updatedDocument.getStatus().name());
        params.add(SYMBOL_PARAM, updatedDocument.getSymbol().name());
        params.add(TYPE_PARAM, updatedDocument.getType().name());
        params.add(ANNULLED_PARAM, TRUE);
        params.add(SECRET_PARAM, FALSE);
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

    public static final long DOCUMENT_1_CONTENT_1_ID = 100022;
    public static final long DOCUMENT_1_CONTENT_2_ID = 100023;
    public static final long DOCUMENT_1_CONTENT_3_ID = 100024;

    public static final String CONTENT_TEST_DATA_DIR_NAME = "test-data";
    public static final String NOT_EXISTED_CONTENT_FILE = NOT_EXISTED_DECIMAL_NUMBER + ".pdf";
    public static final String NOT_EXISTED_CONTENT_FILE_LINK = NOT_EXISTED_DECIMAL_NUMBER + "/0/" + NOT_EXISTED_CONTENT_FILE;
    public static final String NEW_CONTENT_FILE = "VUIA.465521.004.txt";
    public static final String NEW_CONTENT_FILE_LINK = "VUIA.465521.004/%s/" + NEW_CONTENT_FILE;
    public static final String NOT_EXISTED_CONTENT_CHANGE_NUMBER = "3";

    public static final MatcherFactory.Matcher<Content> CONTENT_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(Content.class, "document", "created");

    public static final Content content1 = new Content(DOCUMENT_1_CONTENT_1_ID, 0, LocalDateTime.of(2023, FEBRUARY, 5, 12, 10, 0),
            document1, List.of(new ContentFile("VUIA.465521.004.docx", "VUIA.465521.004/0/VUIA.465521.004.docx"),
            new ContentFile("VUIA.465521.004.pdf", "VUIA.465521.004/0/VUIA.465521.004.pdf")));

    public static final Content content2 = new Content(DOCUMENT_1_CONTENT_2_ID, 1, LocalDateTime.of(2023, MAY, 18, 14, 5, 0),
            document1, List.of(new ContentFile("VUIA.465521.004.docx", "VUIA.465521.004/1/VUIA.465521.004.docx")));

    public static final Content content3 = new Content(DOCUMENT_1_CONTENT_3_ID, 2, LocalDateTime.of(2023, JULY, 24, 9, 28, 0),
            document1, List.of(new ContentFile("VUIA.465521.004.pdf", "VUIA.465521.004/2/VUIA.465521.004.pdf")));

    public static final MockMultipartFile CONTENT_FILE = new MockMultipartFile(FILES_PARAM, "VUIA.465521.004.txt",
            MediaType.TEXT_PLAIN_VALUE, "VUIA.465521.004 content change num 3".getBytes());


    public static final long DOCUMENT_1_SUBSCRIBER_1_ID = 100026;
    public static final long DOCUMENT_1_SUBSCRIBER_2_ID = 100027;
    public static final long DOCUMENT_1_SUBSCRIBER_3_ID = 100028;

    public static final MatcherFactory.Matcher<Subscriber> SUBSCRIBER_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(Subscriber.class, "document", "company", "unsubscribeTimestamp");

    public static final Subscriber subscriber1 = new Subscriber(DOCUMENT_1_SUBSCRIBER_1_ID, document1, company1, true, DUPLICATE);
    public static final Subscriber subscriber2 = new Subscriber(DOCUMENT_1_SUBSCRIBER_2_ID, document1, company2, false, UNACCOUNTED_COPY);
    public static final Subscriber subscriber3 = new Subscriber(DOCUMENT_1_SUBSCRIBER_3_ID, document1, company3, false, DUPLICATE,
            LocalDateTime.of(2022, OCTOBER, 14, 11, 35), "Letter # 2368-456 dated 2022-09-25");


    public static final long DOCUMENT_1_COMPANY_1_SENDING_1_ID = 100041;
    public static final long DOCUMENT_1_COMPANY_1_SENDING_2_ID = 100042;

    public static final MatcherFactory.Matcher<Sending> SENDING_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(Sending.class, "document", "invoice.letter.company");


    public static final Sending sending1 = new Sending(DOCUMENT_1_COMPANY_1_SENDING_1_ID, document1,
            new Invoice(100036L, "75", LocalDate.of(2018, MARCH, 16), ACCOUNTED_COPY,
                    new Letter(100031L, null, null, company1)));

    public static final Sending sending2 = new Sending(DOCUMENT_1_COMPANY_1_SENDING_2_ID, document1,
            new Invoice(100037L, "84", LocalDate.of(2019, FEBRUARY, 12), DUPLICATE,
                    new Letter(100032L, "15/49-3256", LocalDate.of(2019, FEBRUARY, 14), company1)));

    public static SendingTo getNewSendingTo() {
        return new SendingTo(null, DOCUMENT1_ID, COMPANY1_ID, DUPLICATE, "100", LocalDate.of(2023, NOVEMBER, 11), "15/49-777", LocalDate.of(2023, NOVEMBER, 11));
    }
}
