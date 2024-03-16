package ru.javaprojects.archivist.tools.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.javaprojects.archivist.AbstractControllerTest;
import ru.javaprojects.archivist.common.error.IllegalRequestDataException;
import ru.javaprojects.archivist.common.error.NotFoundException;
import ru.javaprojects.archivist.documents.model.Invoice;
import ru.javaprojects.archivist.documents.model.Status;
import ru.javaprojects.archivist.documents.model.Subscriber;
import ru.javaprojects.archivist.documents.repository.InvoiceRepository;
import ru.javaprojects.archivist.documents.repository.SendingRepository;
import ru.javaprojects.archivist.documents.repository.SubscriberRepository;

import java.time.LocalDate;
import java.util.Objects;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.javaprojects.archivist.CommonTestData.*;
import static ru.javaprojects.archivist.common.web.PathUIController.LOGIN_URL;
import static ru.javaprojects.archivist.companies.CompanyTestData.*;
import static ru.javaprojects.archivist.documents.DocumentTestData.*;
import static ru.javaprojects.archivist.tools.ToolsTestData.*;
import static ru.javaprojects.archivist.tools.web.ToolUIController.TOOLS_URL;

class ToolRestControllerTest extends AbstractControllerTest {
    private static final String GROUP_SENDING_ADD_URL = TOOLS_URL + "/group/sending/add";

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private SendingRepository sendingRepository;

    @Autowired
    private SubscriberRepository subscriberRepository;

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createGroupSending() throws Exception {
        MultiValueMap<String, String> newGroupSendingParams = getNewGroupSendingParams();
        ResultActions actions = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, GROUP_SENDING_ADD_URL)
                .file(DECIMAL_NUMBERS_LIST_FILE)
                .params(newGroupSendingParams)
                .with(csrf()))
                .andExpect(status().isCreated());
        GROUP_ADD_SENDING_RESULT_MATCHER.assertMatch(GROUP_ADD_SENDING_RESULT_MATCHER.readFromJson(actions), result);
        Invoice createdInvoice = invoiceRepository.findByNumberAndDate(newGroupSendingParams.get(INVOICE_NUMBER).get(0),
                LocalDate.parse(newGroupSendingParams.get(INVOICE_DATE).get(0))).orElseThrow();
        assertEquals(newGroupSendingParams.get(LETTER_NUMBER).get(0), createdInvoice.getLetter().getNumber());
        assertEquals(LocalDate.parse(newGroupSendingParams.get(LETTER_DATE).get(0)), createdInvoice.getLetter().getDate());
        assertEquals(result.getSentDocuments().size(), sendingRepository.countAllByInvoice_Id(createdInvoice.id()));
        Consumer<Subscriber> statusChecker = subscriber -> {
            assertTrue(subscriber.isSubscribed());
            assertEquals(Status.DUPLICATE, subscriber.getStatus());
        };
        Runnable notFoundExThrower = () -> {
            throw new NotFoundException("Not found subscriber");
        };
        subscriberRepository.findByDocument_IdAndCompany_Id(DOCUMENT1_ID, COMPANY1_ID).ifPresentOrElse(statusChecker, notFoundExThrower);
        subscriberRepository.findByDocument_IdAndCompany_Id(DOCUMENT2_ID, COMPANY1_ID).ifPresentOrElse(statusChecker, notFoundExThrower);
        subscriberRepository.findByDocument_IdAndCompany_Id(DOCUMENT3_ID, COMPANY1_ID).ifPresentOrElse(statusChecker, notFoundExThrower);
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createGroupSendingWhenCompanyNotExists() throws Exception {
        MultiValueMap<String, String> newGroupSendingParams = getNewGroupSendingParams();
        newGroupSendingParams.set(COMPANY_ID, String.valueOf(NOT_FOUND));
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, GROUP_SENDING_ADD_URL)
                .file(DECIMAL_NUMBERS_LIST_FILE)
                .params(newGroupSendingParams)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(GROUP_SENDING_ADD_URL));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createGroupSendingInvalid() throws Exception {
        MultiValueMap<String, String> newGroupSendingParams = getNewGroupSendingParams();
        newGroupSendingParams.set(INVOICE_NUMBER, "");
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, GROUP_SENDING_ADD_URL)
                .file(DECIMAL_NUMBERS_LIST_FILE)
                .params(newGroupSendingParams)
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        MethodArgumentNotValidException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemInstance(GROUP_SENDING_ADD_URL));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createGroupSendingWithOriginalStatus() throws Exception {
        MultiValueMap<String, String> newGroupSendingParams = getNewGroupSendingParams();
        newGroupSendingParams.set(STATUS, Status.ORIGINAL.name());
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, GROUP_SENDING_ADD_URL)
                .file(DECIMAL_NUMBERS_LIST_FILE)
                .params(newGroupSendingParams)
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        MethodArgumentNotValidException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemInstance(GROUP_SENDING_ADD_URL));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createGroupSendingDuplicateInvoiceWhenDifferentStatus() throws Exception {
        MultiValueMap<String, String> newGroupSendingParams = getNewGroupSendingParams();
        newGroupSendingParams.set(INVOICE_NUMBER, sending1.getInvoice().getNumber());
        newGroupSendingParams.set(INVOICE_DATE, sending1.getInvoice().getDate().toString());
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, GROUP_SENDING_ADD_URL)
                .file(DECIMAL_NUMBERS_LIST_FILE)
                .params(newGroupSendingParams)
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        IllegalRequestDataException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail("specified invoice exists and has " + sending1.getInvoice().getStatus() + " status"))
                .andExpect(problemInstance(GROUP_SENDING_ADD_URL));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createGroupSendingDuplicateInvoiceWhenDifferentCompany() throws Exception {
        MultiValueMap<String, String> newGroupSendingParams = getNewGroupSendingParams();
        newGroupSendingParams.set(INVOICE_NUMBER, sending1.getInvoice().getNumber());
        newGroupSendingParams.set(INVOICE_DATE, sending1.getInvoice().getDate().toString());
        newGroupSendingParams.set(COMPANY_ID, String.valueOf(COMPANY2_ID));
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, GROUP_SENDING_ADD_URL)
                .file(DECIMAL_NUMBERS_LIST_FILE)
                .params(newGroupSendingParams)
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        IllegalRequestDataException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail("specified invoice exists and addressed to " + company1.getName()))
                .andExpect(problemInstance(GROUP_SENDING_ADD_URL));
    }

    @Test
    void createGroupSendingUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, GROUP_SENDING_ADD_URL)
                .file(DECIMAL_NUMBERS_LIST_FILE)
                .params(getNewGroupSendingParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createGroupSendingForbidden() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, GROUP_SENDING_ADD_URL)
                .file(DECIMAL_NUMBERS_LIST_FILE)
                .params(getNewGroupSendingParams())
                .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
