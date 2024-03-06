package ru.javaprojects.archivist.departments.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaprojects.archivist.AbstractControllerTest;
import ru.javaprojects.archivist.common.error.IllegalRequestDataException;
import ru.javaprojects.archivist.common.error.NotFoundException;
import ru.javaprojects.archivist.departments.service.EmployeeService;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.javaprojects.archivist.CommonTestData.*;
import static ru.javaprojects.archivist.common.web.PathUIController.LOGIN_URL;
import static ru.javaprojects.archivist.departments.DepartmentTestData.*;
import static ru.javaprojects.archivist.departments.web.EmployeeUIController.EMPLOYEES_URL;

class EmployeeRestControllerTest extends AbstractControllerTest {
    private static final String EMPLOYEES_URL_SLASH = EMPLOYEES_URL + "/";
    private static final String EMPLOYEES_BY_DEPARTMENT = EMPLOYEES_URL_SLASH + "by-department";

    @Autowired
    private EmployeeService service;

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void getAll() throws Exception {
        perform(MockMvcRequestBuilders.get(EMPLOYEES_BY_DEPARTMENT)
                .param(DEPARTMENT_ID, String.valueOf(DEPARTMENT2_ID))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(EMPLOYEE_MATCHER.contentJsonIgnoreFields(List.of(dep2Employee1, dep2Employee2), "department"));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void getAllWhenDepartmentNotExists() throws Exception {
        perform(MockMvcRequestBuilders.get(EMPLOYEES_BY_DEPARTMENT)
                .param(DEPARTMENT_ID, String.valueOf(NOT_FOUND))
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(EMPLOYEES_BY_DEPARTMENT));
    }

    @Test
    void getAllUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(EMPLOYEES_BY_DEPARTMENT)
                .param(DEPARTMENT_ID, String.valueOf(DEPARTMENT2_ID))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void delete() throws Exception {
        perform(MockMvcRequestBuilders.delete(EMPLOYEES_URL_SLASH + DEP2_EMPLOYEE2_ID)
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertThrows(NotFoundException.class, () -> service.getWithDepartment(DEP2_EMPLOYEE2_ID));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void deleteBoss() throws Exception {
        perform(MockMvcRequestBuilders.delete(EMPLOYEES_URL_SLASH + DEP2_EMPLOYEE1_ID)
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        IllegalRequestDataException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail("Cannot delete boss of the department"))
                .andExpect(problemInstance(EMPLOYEES_URL_SLASH + DEP2_EMPLOYEE1_ID));
        assertDoesNotThrow(() -> service.getWithDepartment(DEP2_EMPLOYEE1_ID));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void deleteNotFound() throws Exception {
        perform(MockMvcRequestBuilders.delete(EMPLOYEES_URL_SLASH + NOT_FOUND)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(EMPLOYEES_URL_SLASH + NOT_FOUND));
    }

    @Test
    void deleteUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.delete(EMPLOYEES_URL_SLASH + DEP2_EMPLOYEE2_ID)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertDoesNotThrow(() -> service.getWithDepartment(DEP2_EMPLOYEE1_ID));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void deleteForbidden() throws Exception {
        perform(MockMvcRequestBuilders.delete(EMPLOYEES_URL_SLASH + DEP2_EMPLOYEE2_ID)
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertDoesNotThrow(() -> service.getWithDepartment(DEP2_EMPLOYEE1_ID));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void fire() throws Exception {
        perform(MockMvcRequestBuilders.patch(EMPLOYEES_URL_SLASH + DEP2_EMPLOYEE1_ID)
                .param(FIRED, String.valueOf(true))
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertTrue(service.getWithDepartment(DEP2_EMPLOYEE1_ID).isFired());

        perform(MockMvcRequestBuilders.patch(EMPLOYEES_URL_SLASH + DEP2_EMPLOYEE1_ID)
                .param(FIRED, String.valueOf(false))
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertFalse(service.getWithDepartment(DEP2_EMPLOYEE1_ID).isFired());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void fireNotFound() throws Exception {
        perform(MockMvcRequestBuilders.patch(EMPLOYEES_URL_SLASH + NOT_FOUND)
                .param(FIRED, String.valueOf(true))
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(EMPLOYEES_URL_SLASH + NOT_FOUND));
    }

    @Test
    void fireUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.patch(EMPLOYEES_URL_SLASH + DEP2_EMPLOYEE1_ID)
                .param(FIRED, String.valueOf(true))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertFalse(service.getWithDepartment(DEP2_EMPLOYEE1_ID).isFired());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void fireForbidden() throws Exception {
        perform(MockMvcRequestBuilders.patch(EMPLOYEES_URL_SLASH + DEP2_EMPLOYEE1_ID)
                .param(FIRED, String.valueOf(true))
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertFalse(service.getWithDepartment(DEP2_EMPLOYEE1_ID).isFired());
    }
}
