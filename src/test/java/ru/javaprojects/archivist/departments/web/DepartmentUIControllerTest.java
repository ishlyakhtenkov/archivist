package ru.javaprojects.archivist.departments.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.MultiValueMap;
import ru.javaprojects.archivist.AbstractControllerTest;
import ru.javaprojects.archivist.common.error.NotFoundException;
import ru.javaprojects.archivist.departments.DepartmentTestData;
import ru.javaprojects.archivist.departments.model.Department;
import ru.javaprojects.archivist.departments.service.DepartmentService;

import java.util.List;
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

class DepartmentUIControllerTest extends AbstractControllerTest {
    private static final String DEPARTMENTS_ADD_FORM_URL = DEPARTMENTS_URL + "/add";
    private static final String DEPARTMENTS_EDIT_FORM_URL = DEPARTMENTS_URL + "/edit/";
    private static final String DEPARTMENTS_DELETE_URL = DEPARTMENTS_URL + "/delete/";
    private static final String DEPARTMENTS_CREATE_URL = DEPARTMENTS_URL + "/create";
    private static final String DEPARTMENTS_UPDATE_URL = DEPARTMENTS_URL + "/update";

    private static final String DEPARTMENTS_VIEW = "departments/departments";
    private static final String DEPARTMENT_VIEW = "departments/department";
    private static final String DEPARTMENT_ADD_FORM_VIEW = "departments/department-add-form";
    private static final String DEPARTMENT_EDIT_FORM_VIEW = "departments/department-edit-form";

    @Autowired
    private DepartmentService service;

    @Test
    @WithUserDetails(USER_MAIL)
    @SuppressWarnings("unchecked")
    void getAll() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(DEPARTMENTS_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(DEPARTMENTS_ATTRIBUTE))
                .andExpect(view().name(DEPARTMENTS_VIEW));
        List<Department> departments = (List<Department>) Objects.requireNonNull(actions.andReturn().getModelAndView())
                .getModel().get(DEPARTMENTS_ATTRIBUTE);
        DEPARTMENT_MATCHER.assertMatchIgnoreFields(departments, List.of(department4, department5, department1, department2, department3), "employees", "boss.department");
    }

    @Test
    void getAllUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(DEPARTMENTS_URL)
                .params(getPageableParams()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void get() throws Exception {
        perform(MockMvcRequestBuilders.get(DEPARTMENTS_URL + "/" + DEPARTMENT2_ID))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(DEPARTMENT_ATTRIBUTE))
                .andExpect(view().name(DEPARTMENT_VIEW))
                .andExpect(result -> DEPARTMENT_MATCHER.assertMatchIgnoreFields((Department) Objects.requireNonNull(result
                        .getModelAndView()).getModel().get(DEPARTMENT_ATTRIBUTE), department2, "employees.department", "boss"));
    }

    @Test
    void getUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(DEPARTMENTS_URL + "/" + DEPARTMENT2_ID))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void showAddForm() throws Exception {
        perform(MockMvcRequestBuilders.get(DEPARTMENTS_ADD_FORM_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(DEPARTMENT_CREATE_TO_ATTRIBUTE))
                .andExpect(view().name(DEPARTMENT_ADD_FORM_VIEW));
    }

    @Test
    void showAddFormUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(DEPARTMENTS_ADD_FORM_URL))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showAddFormForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(DEPARTMENTS_ADD_FORM_URL))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void create() throws Exception {
        Department newDepartment = getNewDepartment();
        perform(MockMvcRequestBuilders.post(DEPARTMENTS_CREATE_URL)
                .params(getNewDepartmentParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(DEPARTMENTS_URL))
                .andExpect(flash().attribute(ACTION, "Department " + newDepartment.getName() + " was created"));
        Department created = service.getByName(newDepartment.getName());
        newDepartment.setId(created.id());
        DEPARTMENT_MATCHER.assertMatchIgnoreFields(created, newDepartment, "employees", "boss.id", "boss.department");
    }

    @Test
    void createUnAuthorize() throws Exception {
        perform(MockMvcRequestBuilders.post(DEPARTMENTS_CREATE_URL)
                .params(getNewDepartmentParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertThrows(NotFoundException.class, () -> service.getByName(getNewDepartment().getName()));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createForbidden() throws Exception {
        perform(MockMvcRequestBuilders.post(DEPARTMENTS_CREATE_URL)
                .params(getNewDepartmentParams())
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertThrows(NotFoundException.class, () -> service.getByName(getNewDepartment().getName()));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createInvalid() throws Exception {
        MultiValueMap<String, String> newInvalidParams = getNewDepartmentInvalidParams();
        perform(MockMvcRequestBuilders.post(DEPARTMENTS_CREATE_URL)
                .params(newInvalidParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(DEPARTMENT_CREATE_TO_ATTRIBUTE, NAME,
                        BOSS_LAST_NAME, BOSS_FIRST_NAME, BOSS_MIDDLE_NAME, BOSS_PHONE))
                .andExpect(view().name(DEPARTMENT_ADD_FORM_VIEW));
        assertThrows(NotFoundException.class, () -> service.getByName(newInvalidParams.get(NAME).get(0)));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createDuplicateName() throws Exception {
        MultiValueMap<String, String> newParams = getNewDepartmentParams();
        newParams.set(NAME, DEPARTMENT1_NAME);
        perform(MockMvcRequestBuilders.post(DEPARTMENTS_CREATE_URL)
                .params(newParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(DEPARTMENT_CREATE_TO_ATTRIBUTE, NAME, DUPLICATE_ERROR_CODE))
                .andExpect(view().name(DEPARTMENT_ADD_FORM_VIEW));
        assertNotEquals(DepartmentTestData.getNewDepartment().getBoss(), service.getByName(DEPARTMENT1_NAME).getBoss());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void showEditForm() throws Exception {
        perform(MockMvcRequestBuilders.get(DEPARTMENTS_EDIT_FORM_URL + DEPARTMENT2_ID))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(DEPARTMENT_UPDATE_TO_ATTRIBUTE))
                .andExpect(model().attributeExists(EMPLOYEES_ATTRIBUTE))
                .andExpect(view().name(DEPARTMENT_EDIT_FORM_VIEW));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void showEditFormNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(DEPARTMENTS_EDIT_FORM_URL + NOT_FOUND))
                .andExpect(exception().exceptionPage(ENTITY_NOT_FOUND, NotFoundException.class));
    }

    @Test
    void showEditFormUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(DEPARTMENTS_EDIT_FORM_URL + DEPARTMENT2_ID))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showEditFormForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(DEPARTMENTS_EDIT_FORM_URL + DEPARTMENT2_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void update() throws Exception {
        Department updatedDepartment = getUpdatedDepartment();
        perform(MockMvcRequestBuilders.post(DEPARTMENTS_UPDATE_URL)
                .params(getUpdatedDepartmentParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(DEPARTMENTS_URL))
                .andExpect(flash().attribute(ACTION, "Department " + updatedDepartment.getName() + " was updated"));
        DEPARTMENT_MATCHER.assertMatchIgnoreFields(service.getWithBoss(DEPARTMENT2_ID), updatedDepartment, "employees", "boss.department");
    }

    //Check UniqueNameValidator works correct when update
    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void updateNameNotChange() throws Exception {
        Department updatedDepartment = getUpdatedDepartment();
        updatedDepartment.setName(DEPARTMENT2_NAME);
        MultiValueMap<String, String> updatedParams = getUpdatedDepartmentParams();
        updatedParams.set(NAME, DEPARTMENT2_NAME);
        perform(MockMvcRequestBuilders.post(DEPARTMENTS_UPDATE_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(DEPARTMENTS_URL))
                .andExpect(flash().attribute(ACTION, "Department " + updatedDepartment.getName() + " was updated"));
        DEPARTMENT_MATCHER.assertMatchIgnoreFields(service.getWithBoss(DEPARTMENT2_ID), updatedDepartment, "employees", "boss.department");
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void updateNotFound() throws Exception {
        MultiValueMap<String, String> updatedParams = getUpdatedDepartmentParams();
        updatedParams.set(ID, String.valueOf(NOT_FOUND));
        perform(MockMvcRequestBuilders.post(DEPARTMENTS_UPDATE_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(exception().exceptionPage(ENTITY_NOT_FOUND, NotFoundException.class));
    }

    @Test
    void updateUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.post(DEPARTMENTS_UPDATE_URL)
                .params(getUpdatedDepartmentParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertNotEquals(service.get(DEPARTMENT2_ID).getName(), DepartmentTestData.getUpdatedDepartment().getName());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateForbidden() throws Exception {
        perform(MockMvcRequestBuilders.post(DEPARTMENTS_UPDATE_URL)
                .params(getUpdatedDepartmentParams())
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertNotEquals(service.get(DEPARTMENT2_ID).getName(), DepartmentTestData.getUpdatedDepartment().getName());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void updateInvalid() throws Exception {
        MultiValueMap<String, String> updatedInvalidParams = getUpdatedDepartmentInvalidParams();
        perform(MockMvcRequestBuilders.post(DEPARTMENTS_UPDATE_URL)
                .params(updatedInvalidParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(DEPARTMENT_UPDATE_TO_ATTRIBUTE, NAME))
                .andExpect(view().name(DEPARTMENT_EDIT_FORM_VIEW));
        assertNotEquals(service.get(DEPARTMENT2_ID).getName(), updatedInvalidParams.get(NAME).get(0));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void updateDuplicateName() throws Exception {
        MultiValueMap<String, String> updatedParams = getUpdatedDepartmentParams();
        updatedParams.set(NAME, DEPARTMENT1_NAME);
        perform(MockMvcRequestBuilders.post(DEPARTMENTS_UPDATE_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(DEPARTMENT_UPDATE_TO_ATTRIBUTE, NAME, DUPLICATE_ERROR_CODE))
                .andExpect(view().name(DEPARTMENT_EDIT_FORM_VIEW));
        assertNotEquals(service.get(DEPARTMENT2_ID).getName(), DEPARTMENT1_NAME);
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void delete() throws Exception {
        perform(MockMvcRequestBuilders.post(DEPARTMENTS_DELETE_URL + DEPARTMENT5_ID)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(DEPARTMENTS_URL))
                .andExpect(flash().attribute(ACTION, "Department " + DEPARTMENT5_NAME + " was deleted"));
        assertThrows(NotFoundException.class, () -> service.get(DEPARTMENT5_ID));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void deleteWhenDocumentsHasReference() throws Exception {
        perform(MockMvcRequestBuilders.post(DEPARTMENTS_DELETE_URL + DEPARTMENT2_ID)
                .with(csrf()))
                .andExpect(status().isInternalServerError());
        assertDoesNotThrow(() -> service.get(DEPARTMENT2_ID));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void deleteNotFound() throws Exception {
        perform(MockMvcRequestBuilders.post(DEPARTMENTS_DELETE_URL + NOT_FOUND)
                .with(csrf()))
                .andExpect(exception().exceptionPage(ENTITY_NOT_FOUND, NotFoundException.class));
    }

    @Test
    void deleteUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.post(DEPARTMENTS_DELETE_URL + DEPARTMENT1_ID)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertDoesNotThrow(() -> service.get(DEPARTMENT1_ID));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void deleteForbidden() throws Exception {
        perform(MockMvcRequestBuilders.post(DEPARTMENTS_DELETE_URL + DEPARTMENT1_ID)
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertDoesNotThrow(() -> service.get(DEPARTMENT1_ID));
    }
}
