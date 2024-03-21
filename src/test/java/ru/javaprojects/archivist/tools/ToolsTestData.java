package ru.javaprojects.archivist.tools;

import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.javaprojects.archivist.MatcherFactory;
import ru.javaprojects.archivist.documents.model.Status;

import java.time.LocalDate;
import java.util.List;

import static java.time.Month.NOVEMBER;
import static ru.javaprojects.archivist.CommonTestData.COMPANY_ID;
import static ru.javaprojects.archivist.CommonTestData.STATUS;
import static ru.javaprojects.archivist.companies.CompanyTestData.COMPANY1_ID;
import static ru.javaprojects.archivist.documents.DocumentTestData.*;

public class ToolsTestData {
    public static final MatcherFactory.Matcher<GroupAddSendingResult> GROUP_ADD_SENDING_RESULT_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(GroupAddSendingResult.class);

    public static final MatcherFactory.Matcher<GroupDeleteSendingResult> GROUP_DELETE_SENDING_RESULT_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(GroupDeleteSendingResult.class);

    public static final MatcherFactory.Matcher<GroupUnsubscribeResult> GROUP_UNSUBSCRIBE_RESULT_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(GroupUnsubscribeResult.class);

    public static final String INVOICE_NUMBER = "invoiceNumber";
    public static final String INVOICE_DATE = "invoiceDate";
    public static final String LETTER_NUMBER = "letterNumber";
    public static final String LETTER_DATE = "letterDate";

    public static MultiValueMap<String, String> getGroupAddSendingParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(COMPANY_ID, String.valueOf(COMPANY1_ID));
        params.add(STATUS, Status.DUPLICATE.name());
        params.add(INVOICE_NUMBER, "100");
        params.add(INVOICE_DATE,  LocalDate.of(2023, NOVEMBER, 11).toString());
        params.add(LETTER_NUMBER, "15/49-777");
        params.add(LETTER_DATE, LocalDate.of(2023, NOVEMBER, 11).toString());
        return params;
    }

    public static final MockMultipartFile DECIMAL_NUMBERS_LIST_FILE = new MockMultipartFile("file", "doc_list.txt",
            MediaType.TEXT_PLAIN_VALUE,
            (String.join("\n", document1.getDecimalNumber(), document2.getDecimalNumber(),
                    document3.getDecimalNumber(), NOT_EXISTING_DECIMAL_NUMBER, AUTO_GENERATED_DECIMAL_NUMBER)).getBytes());

    public static final GroupAddSendingResult groupAddSendingResult =
            new GroupAddSendingResult(List.of(NOT_EXISTING_DECIMAL_NUMBER, AUTO_GENERATED_DECIMAL_NUMBER),
                    List.of(), List.of(document3.getDecimalNumber(), document1.getDecimalNumber(), document2.getDecimalNumber()));

    public static MultiValueMap<String, String> getGroupDeleteSendingParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(STATUS, Status.ACCOUNTED_COPY.name());
        params.add(INVOICE_NUMBER, sending1.getInvoice().getNumber());
        params.add(INVOICE_DATE,  sending1.getInvoice().getDate().toString());
        return params;
    }

    public static final GroupDeleteSendingResult groupDeleteSendingResult =
            new GroupDeleteSendingResult(List.of(document3.getDecimalNumber(), NOT_EXISTING_DECIMAL_NUMBER,
                    AUTO_GENERATED_DECIMAL_NUMBER), List.of(document1.getDecimalNumber(), document2.getDecimalNumber()));

    public static MultiValueMap<String, String> getGroupUnsubscribeParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(COMPANY_ID, String.valueOf(COMPANY1_ID));
        params.add(UNSUBSCRIBE_REASON, REASON_FOR_UNSUBSCRIBE);
        return params;
    }

    public static final GroupUnsubscribeResult groupUnsubscribeResult =
            new GroupUnsubscribeResult(List.of(document3.getDecimalNumber(), NOT_EXISTING_DECIMAL_NUMBER,
                    AUTO_GENERATED_DECIMAL_NUMBER), List.of(document1.getDecimalNumber(), document2.getDecimalNumber()));
}
