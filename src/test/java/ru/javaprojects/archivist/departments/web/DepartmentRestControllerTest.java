package ru.javaprojects.archivist.departments.web;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaprojects.archivist.AbstractControllerTest;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.javaprojects.archivist.CommonTestData.ARCHIVIST_MAIL;
import static ru.javaprojects.archivist.common.web.PathUIController.LOGIN_URL;
import static ru.javaprojects.archivist.departments.DepartmentTestData.*;
import static ru.javaprojects.archivist.departments.web.DepartmentUIController.DEPARTMENTS_URL;

class DepartmentRestControllerTest extends AbstractControllerTest {
    private static final String DEPARTMENTS_LIST_URL = DEPARTMENTS_URL + "/" + "list";

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void getAll() throws Exception {
        perform(MockMvcRequestBuilders.get(DEPARTMENTS_LIST_URL)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(DEPARTMENT_MATCHER.contentJsonIgnoreFields(
                        List.of(department4, department5, department1, department2, department3), "employees", "boss"));
    }

    @Test
    void getAllUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(DEPARTMENTS_LIST_URL)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }
}
