package ru.javaprojects.archivist.documents.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.javaprojects.archivist.AbstractControllerTest;
import ru.javaprojects.archivist.common.error.NotFoundException;
import ru.javaprojects.archivist.documents.model.Applicability;
import ru.javaprojects.archivist.documents.repository.ApplicabilityRepository;
import ru.javaprojects.archivist.documents.to.ApplicabilityTo;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.javaprojects.archivist.CommonTestData.*;
import static ru.javaprojects.archivist.common.error.Constants.DUPLICATE_DOCUMENT_APPLICABILITY_MESSAGE;
import static ru.javaprojects.archivist.common.error.Constants.DUPLICATE_PRIMAL_APPLICABILITY_MESSAGE;
import static ru.javaprojects.archivist.common.util.JsonUtil.writeValue;
import static ru.javaprojects.archivist.documents.DocumentTestData.*;
import static ru.javaprojects.archivist.documents.web.DocumentUIController.DOCUMENTS_URL;
import static ru.javaprojects.archivist.users.web.LoginController.LOGIN_URL;

class DocumentRestControllerTest extends AbstractControllerTest {
    private static final String APPLICABILITIES_URL = DOCUMENTS_URL + "/applicabilities";
    private static final String APPLICABILITIES_URL_SLASH = DOCUMENTS_URL + "/applicabilities/";

    @Autowired
    private ApplicabilityRepository applicabilityRepository;

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createApplicability() throws Exception {
        ResultActions action = perform(MockMvcRequestBuilders.post(APPLICABILITIES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(getNewApplicabilityTo()))
                .with(csrf()))
                .andExpect(status().isCreated());
        Applicability created = APPLICABILITY_MATCHER.readFromJson(action);
        Applicability newApplicability = new Applicability(created.getId(), document3, document1, false);
        APPLICABILITY_MATCHER.assertMatch(created, newApplicability);
        APPLICABILITY_MATCHER.assertMatch(applicabilityRepository.getExisted(created.id()), newApplicability);
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createApplicabilityWhenDocumentNotExists() throws Exception {
        ApplicabilityTo newApplicabilityTo = getNewApplicabilityTo();
        newApplicabilityTo.setDocumentId(NOT_FOUND);
        perform(MockMvcRequestBuilders.post(APPLICABILITIES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(newApplicabilityTo))
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(APPLICABILITIES_URL));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createApplicabilityInvalid() throws Exception {
        ApplicabilityTo newApplicabilityTo = getNewApplicabilityTo();
        newApplicabilityTo.setDecimalNumber(null);
        perform(MockMvcRequestBuilders.post(APPLICABILITIES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(newApplicabilityTo))
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        MethodArgumentNotValidException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemInstance(APPLICABILITIES_URL));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    @Transactional(propagation = Propagation.NEVER)
    void createApplicabilityDuplicate() throws Exception {
        ApplicabilityTo newApplicabilityTo = new ApplicabilityTo(null, DOCUMENT5_ID, document3.getDecimalNumber(), false);
        perform(MockMvcRequestBuilders.post(APPLICABILITIES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(newApplicabilityTo))
                .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        DataIntegrityViolationException.class))
                .andExpect(problemTitle(HttpStatus.CONFLICT.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.CONFLICT.value()))
                .andExpect(problemDetail(DUPLICATE_DOCUMENT_APPLICABILITY_MESSAGE))
                .andExpect(problemInstance(APPLICABILITIES_URL));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    @Transactional(propagation = Propagation.NEVER)
    void createApplicabilityDuplicatePrimal() throws Exception {
        ApplicabilityTo newApplicabilityTo = new ApplicabilityTo(null, DOCUMENT5_ID, "VUIA.555555.001", true);
        perform(MockMvcRequestBuilders.post(APPLICABILITIES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(newApplicabilityTo))
                .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        DataIntegrityViolationException.class))
                .andExpect(problemTitle(HttpStatus.CONFLICT.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.CONFLICT.value()))
                .andExpect(problemDetail(DUPLICATE_PRIMAL_APPLICABILITY_MESSAGE))
                .andExpect(problemInstance(APPLICABILITIES_URL));
    }

    @Test
    void createApplicabilityUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.post(APPLICABILITIES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(getNewApplicabilityTo()))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createApplicabilityForbidden() throws Exception {
        perform(MockMvcRequestBuilders.post(APPLICABILITIES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(getNewApplicabilityTo()))
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void getApplicabilities() throws Exception {
        perform(MockMvcRequestBuilders.get(DOCUMENTS_URL + "/" + DOCUMENT5_ID + "/applicabilities")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(APPLICABILITY_MATCHER.contentJson(applicability2, applicability1));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void getApplicabilitiesWhenDocumentNotExists() throws Exception {
        perform(MockMvcRequestBuilders.get(DOCUMENTS_URL + "/" + NOT_FOUND + "/applicabilities")
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(DOCUMENTS_URL + "/" + NOT_FOUND + "/applicabilities"));
    }

    @Test
    void getApplicabilitiesUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.patch(DOCUMENTS_URL + "/" + NOT_FOUND + "/applicabilities")
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }


    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void deleteApplicability() throws Exception {
        perform(MockMvcRequestBuilders.delete(APPLICABILITIES_URL_SLASH + DOCUMENT_5_APPLICABILITY_1_ID)
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertThrows(NotFoundException.class, () -> applicabilityRepository.getExisted(DOCUMENT_5_APPLICABILITY_1_ID));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void deleteNotFound() throws Exception {
        perform(MockMvcRequestBuilders.delete(APPLICABILITIES_URL_SLASH + NOT_FOUND)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(APPLICABILITIES_URL_SLASH + NOT_FOUND));
    }

    @Test
    void deleteUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.delete(APPLICABILITIES_URL_SLASH + DOCUMENT_5_APPLICABILITY_1_ID)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertDoesNotThrow(() -> applicabilityRepository.getExisted(DOCUMENT_5_APPLICABILITY_1_ID));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void deleteForbidden() throws Exception {
        perform(MockMvcRequestBuilders.delete(APPLICABILITIES_URL_SLASH + DOCUMENT_5_APPLICABILITY_1_ID)
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertDoesNotThrow(() -> applicabilityRepository.getExisted(DOCUMENT_5_APPLICABILITY_1_ID));
    }
}
