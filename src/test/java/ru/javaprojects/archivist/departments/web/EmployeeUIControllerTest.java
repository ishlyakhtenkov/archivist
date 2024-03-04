package ru.javaprojects.archivist.departments.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.MultiValueMap;
import ru.javaprojects.archivist.AbstractControllerTest;
import ru.javaprojects.archivist.common.error.NotFoundException;
import ru.javaprojects.archivist.departments.DepartmentTestData;
import ru.javaprojects.archivist.departments.model.Employee;
import ru.javaprojects.archivist.departments.service.EmployeeService;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.archivist.AbstractControllerTest.ExceptionResultMatchers.exception;
import static ru.javaprojects.archivist.CommonTestData.*;
import static ru.javaprojects.archivist.common.error.Constants.DUPLICATE_ERROR_CODE;
import static ru.javaprojects.archivist.common.web.PathUIController.LOGIN_URL;
import static ru.javaprojects.archivist.departments.DepartmentTestData.*;
import static ru.javaprojects.archivist.departments.web.DepartmentUIController.DEPARTMENTS_URL;
import static ru.javaprojects.archivist.departments.web.EmployeeUIController.EMPLOYEES_URL;

class EmployeeUIControllerTest extends AbstractControllerTest {
    private static final String EMPLOYEES_ADD_FORM_URL = EMPLOYEES_URL + "/add";
    private static final String EMPLOYEES_EDIT_FORM_URL = EMPLOYEES_URL + "/edit/";
    private static final String EMPLOYEES_FORM_VIEW = "departments/employees/employee-form";

    @Autowired
    private EmployeeService service;

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void showAddForm() throws Exception {
        perform(MockMvcRequestBuilders.get(EMPLOYEES_ADD_FORM_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(EMPLOYEE_ATTRIBUTE))
                .andExpect(model().attributeExists(DEPARTMENTS_ATTRIBUTE))
                .andExpect(view().name(EMPLOYEES_FORM_VIEW))
                .andExpect(result -> EMPLOYEE_MATCHER.assertMatch((Employee) Objects.requireNonNull(result
                        .getModelAndView()).getModel().get(EMPLOYEE_ATTRIBUTE), new Employee()));
    }

    @Test
    void showAddFormUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(EMPLOYEES_ADD_FORM_URL))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showAddFormForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(EMPLOYEES_ADD_FORM_URL))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void create() throws Exception {
        Employee newEmployee = getNewEmployee();
        perform(MockMvcRequestBuilders.post(EMPLOYEES_URL)
                .params((DepartmentTestData.getNewEmployeeParams()))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(DEPARTMENTS_URL + "/" + DEPARTMENT2_ID))
                .andExpect(flash().attribute(ACTION, "Employee " + newEmployee.getFullName() + " was created"));
        Employee created = service.getByEmail(newEmployee.getEmail());
        newEmployee.setId(created.id());
        EMPLOYEE_MATCHER.assertMatchIgnoreFields(created, newEmployee, "department");
    }

    @Test
    void createUnAuthorize() throws Exception {
        perform(MockMvcRequestBuilders.post(EMPLOYEES_URL)
                .params((DepartmentTestData.getNewEmployeeParams()))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertThrows(NotFoundException.class, () -> service.getByEmail(DepartmentTestData.getNewEmployee().getEmail()));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createForbidden() throws Exception {
        perform(MockMvcRequestBuilders.post(EMPLOYEES_URL)
                .params((DepartmentTestData.getNewEmployeeParams()))
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertThrows(NotFoundException.class, () -> service.getByEmail(DepartmentTestData.getNewEmployee().getEmail()));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createInvalid() throws Exception {
        MultiValueMap<String, String> newInvalidParams = DepartmentTestData.getNewEmployeeInvalidParams();
        perform(MockMvcRequestBuilders.post(EMPLOYEES_URL)
                .params(newInvalidParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(EMPLOYEE_ATTRIBUTE, LAST_NAME, FIRST_NAME, MIDDLE_NAME, PHONE, EMAIL))
                .andExpect(view().name(EMPLOYEES_FORM_VIEW));
        assertThrows(NotFoundException.class, () -> service.getByEmail(newInvalidParams.get(EMAIL).get(0)));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createDuplicateEmail() throws Exception {
        MultiValueMap<String, String> newParams = DepartmentTestData.getNewEmployeeParams();
        newParams.set(EMAIL, dep2Employee1.getEmail());
        perform(MockMvcRequestBuilders.post(EMPLOYEES_URL)
                .params(newParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(EMPLOYEE_ATTRIBUTE, EMAIL, DUPLICATE_ERROR_CODE))
                .andExpect(view().name(EMPLOYEES_FORM_VIEW));
        assertNotEquals(DepartmentTestData.getNewEmployee().getFullName(), service.getByEmail(dep2Employee1.getEmail()).getFullName());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void showEditForm() throws Exception {
        perform(MockMvcRequestBuilders.get(EMPLOYEES_EDIT_FORM_URL + DEP2_EMPLOYEE1_ID))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(EMPLOYEE_ATTRIBUTE))
                .andExpect(model().attributeExists(DEPARTMENTS_ATTRIBUTE))
                .andExpect(view().name(EMPLOYEES_FORM_VIEW))
                .andExpect(result -> EMPLOYEE_MATCHER.assertMatchIgnoreFields((Employee) Objects.requireNonNull(result
                        .getModelAndView()).getModel().get(EMPLOYEE_ATTRIBUTE), dep2Employee1, "department.employees"));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void showEditFormNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(EMPLOYEES_EDIT_FORM_URL + NOT_FOUND))
                .andExpect(exception().exceptionPage(ENTITY_NOT_FOUND, NotFoundException.class));
    }

    @Test
    void showEditFormUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(EMPLOYEES_EDIT_FORM_URL + DEP2_EMPLOYEE1_ID))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showEditFormForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(EMPLOYEES_EDIT_FORM_URL + DEP2_EMPLOYEE1_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void update() throws Exception {
        Employee updatedEmployee = DepartmentTestData.getUpdatedEmployee();
        perform(MockMvcRequestBuilders.post(EMPLOYEES_URL)
                .params(DepartmentTestData.getUpdatedEmployeeParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(DEPARTMENTS_URL + "/" + DEPARTMENT1_ID))
                .andExpect(flash().attribute(ACTION, "Employee " + updatedEmployee.getFullName() + " was updated"));
        EMPLOYEE_MATCHER.assertMatchIgnoreFields(service.get(DEP2_EMPLOYEE1_ID), updatedEmployee, "department.employees",
                "department.boss");
    }

    //Check UniqueEmailValidator works correct when update
    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void updateEmailNotChange() throws Exception {
        Employee updatedEmployee = DepartmentTestData.getUpdatedEmployee();
        updatedEmployee.setEmail(dep2Employee1.getEmail());
        MultiValueMap<String, String> updatedParams = DepartmentTestData.getUpdatedEmployeeParams();
        updatedParams.set(EMAIL, dep2Employee1.getEmail());
        perform(MockMvcRequestBuilders.post(EMPLOYEES_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(DEPARTMENTS_URL + "/" + DEPARTMENT1_ID))
                .andExpect(flash().attribute(ACTION, "Employee " + updatedEmployee.getFullName() + " was updated"));
        EMPLOYEE_MATCHER.assertMatchIgnoreFields(service.get(DEP2_EMPLOYEE1_ID), updatedEmployee, "department.employees",
                "department.boss");
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void updateNotFound() throws Exception {
        MultiValueMap<String, String> updatedParams = DepartmentTestData.getUpdatedEmployeeParams();
        updatedParams.set(ID, String.valueOf(NOT_FOUND));
        perform(MockMvcRequestBuilders.post(EMPLOYEES_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(exception().exceptionPage(ENTITY_NOT_FOUND, NotFoundException.class));
    }

    @Test
    void updateUnAuthorize() throws Exception {
        perform(MockMvcRequestBuilders.post(EMPLOYEES_URL)
                .params(DepartmentTestData.getUpdatedEmployeeParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertNotEquals(service.get(DEP2_EMPLOYEE1_ID).getFullName(), DepartmentTestData.getUpdatedEmployee().getFullName());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateForbidden() throws Exception {
        perform(MockMvcRequestBuilders.post(EMPLOYEES_URL)
                .params(DepartmentTestData.getUpdatedEmployeeParams())
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertNotEquals(service.get(DEP2_EMPLOYEE1_ID).getFullName(), DepartmentTestData.getUpdatedEmployee().getFullName());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void updateInvalid() throws Exception {
        MultiValueMap<String, String> updatedInvalidParams = DepartmentTestData.getUpdatedEmployeeInvalidParams();
        perform(MockMvcRequestBuilders.post(EMPLOYEES_URL)
                .params(updatedInvalidParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(EMPLOYEE_ATTRIBUTE, LAST_NAME, FIRST_NAME, MIDDLE_NAME, PHONE, EMAIL))
                .andExpect(view().name(EMPLOYEES_FORM_VIEW));
        assertNotEquals(service.get(DEP2_EMPLOYEE1_ID).getLastName(), updatedInvalidParams.get(LAST_NAME).get(0));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void updateDuplicateEmail() throws Exception {
        MultiValueMap<String, String> updatedParams = DepartmentTestData.getUpdatedEmployeeParams();
        updatedParams.set(EMAIL, dep2Employee2.getEmail());
        perform(MockMvcRequestBuilders.post(EMPLOYEES_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(EMPLOYEE_ATTRIBUTE, EMAIL, DUPLICATE_ERROR_CODE))
                .andExpect(view().name(EMPLOYEES_FORM_VIEW));
        assertNotEquals(service.get(DEP2_EMPLOYEE1_ID).getEmail(), dep2Employee2.getEmail());
    }
}