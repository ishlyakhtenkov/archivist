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
import ru.javaprojects.archivist.documents.repository.LetterRepository;
import ru.javaprojects.archivist.documents.repository.SendingRepository;
import ru.javaprojects.archivist.documents.repository.SubscriberRepository;

import java.time.LocalDate;
import java.util.Objects;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.javaprojects.archivist.CommonTestData.*;
import static ru.javaprojects.archivist.common.web.PathUIController.LOGIN_URL;
import static ru.javaprojects.archivist.companies.CompanyTestData.*;
import static ru.javaprojects.archivist.documents.DocumentTestData.*;
import static ru.javaprojects.archivist.tools.ToolsTestData.*;
import static ru.javaprojects.archivist.tools.web.ToolUIController.TOOLS_URL;

class ToolRestControllerTest extends AbstractControllerTest {
    private static final String GROUP_ADD_SENDING_URL = TOOLS_URL + "/group/sending/add";
    private static final String GROUP_DELETE_SENDING_URL = TOOLS_URL + "/group/sending/delete";

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private SendingRepository sendingRepository;

    @Autowired
    private SubscriberRepository subscriberRepository;

    @Autowired
    private LetterRepository letterRepository;

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createGroupSending() throws Exception {
        MultiValueMap<String, String> newGroupSendingParams = getGroupAddSendingParams();
        ResultActions actions = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, GROUP_ADD_SENDING_URL)
                .file(DECIMAL_NUMBERS_LIST_FILE)
                .params(newGroupSendingParams)
                .with(csrf()))
                .andExpect(status().isCreated());
        GROUP_ADD_SENDING_RESULT_MATCHER.assertMatch(GROUP_ADD_SENDING_RESULT_MATCHER.readFromJson(actions), groupAddSendingResult);
        Invoice createdInvoice = invoiceRepository.findByNumberAndDate(newGroupSendingParams.get(INVOICE_NUMBER).get(0),
                LocalDate.parse(newGroupSendingParams.get(INVOICE_DATE).get(0))).orElseThrow();
        assertEquals(newGroupSendingParams.get(LETTER_NUMBER).get(0), createdInvoice.getLetter().getNumber());
        assertEquals(LocalDate.parse(newGroupSendingParams.get(LETTER_DATE).get(0)), createdInvoice.getLetter().getDate());
        assertEquals(groupAddSendingResult.getSentDocuments().size(), sendingRepository.countAllByInvoice_Id(createdInvoice.id()));
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
        MultiValueMap<String, String> newGroupSendingParams = getGroupAddSendingParams();
        newGroupSendingParams.set(COMPANY_ID, String.valueOf(NOT_FOUND));
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, GROUP_ADD_SENDING_URL)
                .file(DECIMAL_NUMBERS_LIST_FILE)
                .params(newGroupSendingParams)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(GROUP_ADD_SENDING_URL));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createGroupSendingInvalid() throws Exception {
        MultiValueMap<String, String> newGroupSendingParams = getGroupAddSendingParams();
        newGroupSendingParams.set(INVOICE_NUMBER, "");
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, GROUP_ADD_SENDING_URL)
                .file(DECIMAL_NUMBERS_LIST_FILE)
                .params(newGroupSendingParams)
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        MethodArgumentNotValidException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemInstance(GROUP_ADD_SENDING_URL));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createGroupSendingWithOriginalStatus() throws Exception {
        MultiValueMap<String, String> newGroupSendingParams = getGroupAddSendingParams();
        newGroupSendingParams.set(STATUS, Status.ORIGINAL.name());
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, GROUP_ADD_SENDING_URL)
                .file(DECIMAL_NUMBERS_LIST_FILE)
                .params(newGroupSendingParams)
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        MethodArgumentNotValidException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemInstance(GROUP_ADD_SENDING_URL));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createGroupSendingDuplicateInvoiceWhenDifferentStatus() throws Exception {
        MultiValueMap<String, String> newGroupSendingParams = getGroupAddSendingParams();
        newGroupSendingParams.set(INVOICE_NUMBER, sending1.getInvoice().getNumber());
        newGroupSendingParams.set(INVOICE_DATE, sending1.getInvoice().getDate().toString());
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, GROUP_ADD_SENDING_URL)
                .file(DECIMAL_NUMBERS_LIST_FILE)
                .params(newGroupSendingParams)
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        IllegalRequestDataException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail("specified invoice exists and has " + sending1.getInvoice().getStatus() + " status"))
                .andExpect(problemInstance(GROUP_ADD_SENDING_URL));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createGroupSendingDuplicateInvoiceWhenDifferentCompany() throws Exception {
        MultiValueMap<String, String> newGroupSendingParams = getGroupAddSendingParams();
        newGroupSendingParams.set(INVOICE_NUMBER, sending1.getInvoice().getNumber());
        newGroupSendingParams.set(INVOICE_DATE, sending1.getInvoice().getDate().toString());
        newGroupSendingParams.set(COMPANY_ID, String.valueOf(COMPANY2_ID));
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, GROUP_ADD_SENDING_URL)
                .file(DECIMAL_NUMBERS_LIST_FILE)
                .params(newGroupSendingParams)
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        IllegalRequestDataException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail("specified invoice exists and addressed to " + company1.getName()))
                .andExpect(problemInstance(GROUP_ADD_SENDING_URL));
    }

    @Test
    void createGroupSendingUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, GROUP_ADD_SENDING_URL)
                .file(DECIMAL_NUMBERS_LIST_FILE)
                .params(getGroupAddSendingParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createGroupSendingForbidden() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, GROUP_ADD_SENDING_URL)
                .file(DECIMAL_NUMBERS_LIST_FILE)
                .params(getGroupAddSendingParams())
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void deleteGroupSending() throws Exception {
        MultiValueMap<String, String> groupDeleteSendingParams = getGroupDeleteSendingParams();
        ResultActions actions = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, GROUP_DELETE_SENDING_URL)
                .file(DECIMAL_NUMBERS_LIST_FILE)
                .params(groupDeleteSendingParams)
                .with(csrf()))
                .andExpect(status().isOk());
        GROUP_DELETE_SENDING_RESULT_MATCHER.assertMatch(GROUP_DELETE_SENDING_RESULT_MATCHER.readFromJson(actions),
                groupDeleteSendingResult);

        assertEquals(0,
                sendingRepository.countAllByInvoice_Id(Long.parseLong(groupDeleteSendingParams.get(INVOICE_NUMBER).get(0))));
        assertTrue(invoiceRepository.findByNumberAndDate(groupDeleteSendingParams.get(INVOICE_NUMBER).get(0),
                LocalDate.parse(groupDeleteSendingParams.get(INVOICE_DATE).get(0))).isEmpty());
        assertThrows(NotFoundException.class, () -> letterRepository.getExisted(100031));
        assertTrue(subscriberRepository.findByDocument_IdAndCompany_Id(DOCUMENT2_ID, COMPANY1_ID).isEmpty());
        Subscriber document1Subscriber = subscriberRepository.findByDocument_IdAndCompany_Id(DOCUMENT1_ID, COMPANY1_ID)
                .orElseThrow();
        assertTrue(document1Subscriber.isSubscribed());
        assertSame(document1Subscriber.getStatus(), Status.DUPLICATE);
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void deleteGroupSendingWhenInvoiceNotExists() throws Exception {
        MultiValueMap<String, String> groupDeleteSendingParams = getGroupDeleteSendingParams();
        groupDeleteSendingParams.set(INVOICE_NUMBER, String.valueOf(NOT_FOUND));
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, GROUP_DELETE_SENDING_URL)
                .file(DECIMAL_NUMBERS_LIST_FILE)
                .params(groupDeleteSendingParams)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(String.format("Not found invoice # %s with date %s", NOT_FOUND,
                        groupDeleteSendingParams.get(INVOICE_DATE).get(0))))
                .andExpect(problemInstance(GROUP_DELETE_SENDING_URL));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void deleteGroupSendingInvalid() throws Exception {
        MultiValueMap<String, String> groupDeleteSendingParams = getGroupDeleteSendingParams();
        groupDeleteSendingParams.set(INVOICE_NUMBER, "");
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, GROUP_DELETE_SENDING_URL)
                .file(DECIMAL_NUMBERS_LIST_FILE)
                .params(groupDeleteSendingParams)
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        MethodArgumentNotValidException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemInstance(GROUP_DELETE_SENDING_URL));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void deleteGroupSendingWithOriginalStatus() throws Exception {
        MultiValueMap<String, String> groupDeleteSendingParams = getGroupDeleteSendingParams();
        groupDeleteSendingParams.set(STATUS, Status.ORIGINAL.name());
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, GROUP_DELETE_SENDING_URL)
                .file(DECIMAL_NUMBERS_LIST_FILE)
                .params(groupDeleteSendingParams)
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        MethodArgumentNotValidException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemInstance(GROUP_DELETE_SENDING_URL));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void deleteGroupSendingWhenInvoiceHasDifferentStatus() throws Exception {
        MultiValueMap<String, String> groupDeleteSendingParams = getGroupDeleteSendingParams();
        groupDeleteSendingParams.set(STATUS, Status.DUPLICATE.name());
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, GROUP_DELETE_SENDING_URL)
                .file(DECIMAL_NUMBERS_LIST_FILE)
                .params(groupDeleteSendingParams)
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        IllegalRequestDataException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail("specified invoice has " + sending1.getInvoice().getStatus() + " status"))
                .andExpect(problemInstance(GROUP_DELETE_SENDING_URL));
    }

    @Test
    void deleteGroupSendingUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, GROUP_DELETE_SENDING_URL)
                .file(DECIMAL_NUMBERS_LIST_FILE)
                .params(getGroupDeleteSendingParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void deleteGroupSendingForbidden() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, GROUP_DELETE_SENDING_URL)
                .file(DECIMAL_NUMBERS_LIST_FILE)
                .params(getGroupDeleteSendingParams())
                .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
