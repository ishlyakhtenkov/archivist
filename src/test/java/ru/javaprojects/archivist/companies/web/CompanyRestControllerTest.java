package ru.javaprojects.archivist.companies.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaprojects.archivist.AbstractControllerTest;
import ru.javaprojects.archivist.common.error.NotFoundException;
import ru.javaprojects.archivist.companies.CompanyService;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.javaprojects.archivist.CommonTestData.*;
import static ru.javaprojects.archivist.common.web.PathUIController.LOGIN_URL;
import static ru.javaprojects.archivist.companies.CompanyTestData.*;
import static ru.javaprojects.archivist.companies.web.CompanyUIController.COMPANIES_URL;

public class CompanyRestControllerTest extends AbstractControllerTest {
    private static final String COMPANIES_URL_SLASH = COMPANIES_URL + "/";
    private static final String COMPANIES_LIST_URL = COMPANIES_URL_SLASH + "list";

    @Autowired
    private CompanyService service;

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void getAll() throws Exception {
        perform(MockMvcRequestBuilders.get(COMPANIES_LIST_URL)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(COMPANY_MATCHER.contentJsonIgnoreFields(List.of(company2, company3, company1), "contactPersons"));
    }

    @Test
    void getAllUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(COMPANIES_LIST_URL)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void delete() throws Exception {
        perform(MockMvcRequestBuilders.delete(COMPANIES_URL_SLASH + COMPANY1_ID)
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertThrows(NotFoundException.class, () -> service.getWithContactPersons(COMPANY1_ID));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void deleteWhenDocumentsHasReference() throws Exception {
        perform(MockMvcRequestBuilders.delete(COMPANIES_URL_SLASH + COMPANY2_ID)
                .with(csrf()))
                .andExpect(status().isConflict());
        assertDoesNotThrow(() -> service.getWithContactPersons(COMPANY2_ID));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void deleteNotFound() throws Exception {
        perform(MockMvcRequestBuilders.delete(COMPANIES_URL_SLASH + NOT_FOUND)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(COMPANIES_URL_SLASH + NOT_FOUND));
    }

    @Test
    void deleteUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.delete(COMPANIES_URL_SLASH + COMPANY1_ID)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertDoesNotThrow(() -> service.getWithContactPersons(COMPANY1_ID));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void deleteForbidden() throws Exception {
        perform(MockMvcRequestBuilders.delete(COMPANIES_URL_SLASH + COMPANY1_ID)
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertDoesNotThrow(() -> service.getWithContactPersons(COMPANY1_ID));
    }
}
