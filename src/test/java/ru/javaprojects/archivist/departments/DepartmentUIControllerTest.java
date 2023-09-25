package ru.javaprojects.archivist.departments;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.MultiValueMap;
import ru.javaprojects.archivist.AbstractControllerTest;
import ru.javaprojects.archivist.common.error.exception.NotFoundException;
import ru.javaprojects.archivist.common.model.Person;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.archivist.AbstractControllerTest.ExceptionResultMatchers.exception;
import static ru.javaprojects.archivist.CommonTestData.*;
import static ru.javaprojects.archivist.common.util.validation.Constants.DUPLICATE_ERROR_CODE;
import static ru.javaprojects.archivist.departments.DepartmentTestData.*;
import static ru.javaprojects.archivist.departments.DepartmentUIController.DEPARTMENTS_URL;
import static ru.javaprojects.archivist.users.web.LoginController.LOGIN_URL;

class DepartmentUIControllerTest extends AbstractControllerTest {
    private static final String DEPARTMENTS_ADD_FORM_URL = DEPARTMENTS_URL + "/add";
    private static final String DEPARTMENTS_CREATE_URL = DEPARTMENTS_URL + "/create";
    private static final String DEPARTMENTS_EDIT_FORM_URL = DEPARTMENTS_URL + "/edit/";
    private static final String DEPARTMENTS_UPDATE_URL = DEPARTMENTS_URL + "/update";
    private static final String DEPARTMENTS_DELETE_URL = DEPARTMENTS_URL + "/delete/";

    private static final String DEPARTMENTS_VIEW = "departments/departments";
    private static final String DEPARTMENTS_FORM_VIEW = "departments/department-form";

    @Autowired
    private DepartmentService service;

    @Autowired
    private EntityManager entityManager;

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
        DEPARTMENT_MATCHER.assertMatch(departments, List.of(department4, department5, department1, department2, department3));
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
    @WithUserDetails(ARCHIVIST_MAIL)
    void showAddForm() throws Exception {
        perform(MockMvcRequestBuilders.get(DEPARTMENTS_ADD_FORM_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(DEPARTMENT_ATTRIBUTE))
                .andExpect(view().name(DEPARTMENTS_FORM_VIEW))
                .andExpect(result ->
                        DEPARTMENT_MATCHER.assertMatch((Department) Objects.requireNonNull(result.getModelAndView()).getModel().get(DEPARTMENT_ATTRIBUTE),
                                new Department(null, null, new Person())));
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
        Department newDepartment = DepartmentTestData.getNew();
        perform(MockMvcRequestBuilders.post(DEPARTMENTS_CREATE_URL)
                .params((DepartmentTestData.getNewParams()))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(DEPARTMENTS_URL))
                .andExpect(flash().attribute(ACTION, "Department " + newDepartment.getName() + " was created"));
        Department created = service.getByName(newDepartment.getName());
        newDepartment.setId(created.id());
        DEPARTMENT_MATCHER.assertMatch(created, newDepartment);
    }

    @Test
    void createUnAuthorize() throws Exception {
        perform(MockMvcRequestBuilders.post(DEPARTMENTS_CREATE_URL)
                .params((DepartmentTestData.getNewParams()))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertThrows(NotFoundException.class, () -> service.getByName(DepartmentTestData.getNew().getName()));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createForbidden() throws Exception {
        perform(MockMvcRequestBuilders.post(DEPARTMENTS_CREATE_URL)
                .params((DepartmentTestData.getNewParams()))
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertThrows(NotFoundException.class, () -> service.getByName(DepartmentTestData.getNew().getName()));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createInvalid() throws Exception {
        MultiValueMap<String, String> newInvalidParams = DepartmentTestData.getNewInvalidParams();
        perform(MockMvcRequestBuilders.post(DEPARTMENTS_CREATE_URL)
                .params(newInvalidParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(DEPARTMENT_ATTRIBUTE, NAME_PARAM,
                        BOSS_LAST_NAME_PARAM, BOSS_FIRST_NAME_PARAM, BOSS_MIDDLE_NAME_PARAM, BOSS_PHONE_PARAM))
                .andExpect(view().name(DEPARTMENTS_FORM_VIEW));
        assertThrows(NotFoundException.class, () -> service.getByName(newInvalidParams.get(NAME_PARAM).get(0)));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createDuplicateName() throws Exception {
        MultiValueMap<String, String> newParams = DepartmentTestData.getNewParams();
        newParams.set(NAME_PARAM, DEPARTMENT1_NAME);
        perform(MockMvcRequestBuilders.post(DEPARTMENTS_CREATE_URL)
                .params(newParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(DEPARTMENT_ATTRIBUTE, NAME_PARAM, DUPLICATE_ERROR_CODE))
                .andExpect(view().name(DEPARTMENTS_FORM_VIEW));
        assertNotEquals(DepartmentTestData.getNew().getBoss(), service.getByName(DEPARTMENT1_NAME).getBoss());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void showEditForm() throws Exception {
        perform(MockMvcRequestBuilders.get(DEPARTMENTS_EDIT_FORM_URL + DEPARTMENT1_ID))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(DEPARTMENT_ATTRIBUTE))
                .andExpect(view().name(DEPARTMENTS_FORM_VIEW))
                .andExpect(result ->
                        DEPARTMENT_MATCHER.assertMatch((Department) Objects.requireNonNull(result.getModelAndView()).getModel().get(DEPARTMENT_ATTRIBUTE), department1));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void showEditFormNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(DEPARTMENTS_EDIT_FORM_URL + NOT_FOUND))
                .andExpect(exception().exceptionPage(ENTITY_NOT_FOUND, NotFoundException.class));
    }

    @Test
    void showEditFormUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(DEPARTMENTS_EDIT_FORM_URL + DEPARTMENT1_ID))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showEditFormForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(DEPARTMENTS_EDIT_FORM_URL + DEPARTMENT1_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void update() throws Exception {
        Department updatedDepartment = DepartmentTestData.getUpdated();
        perform(MockMvcRequestBuilders.post(DEPARTMENTS_UPDATE_URL)
                .params(DepartmentTestData.getUpdatedParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(DEPARTMENTS_URL))
                .andExpect(flash().attribute(ACTION, "Department " + updatedDepartment.getName() + " was updated"));
        DEPARTMENT_MATCHER.assertMatch(service.get(DEPARTMENT1_ID), updatedDepartment);
    }

    //Check UniqueNameValidator works correct when update
    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void updateNameNotChange() throws Exception {
        Department updatedDepartment = DepartmentTestData.getUpdated();
        updatedDepartment.setName(DEPARTMENT1_NAME);
        MultiValueMap<String, String> updatedParams = DepartmentTestData.getUpdatedParams();
        updatedParams.set(NAME_PARAM, DEPARTMENT1_NAME);
        perform(MockMvcRequestBuilders.post(DEPARTMENTS_UPDATE_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(DEPARTMENTS_URL))
                .andExpect(flash().attribute(ACTION, "Department " + updatedDepartment.getName() + " was updated"));
        DEPARTMENT_MATCHER.assertMatch(service.get(DEPARTMENT1_ID), updatedDepartment);
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void updateNotFound() throws Exception {
        MultiValueMap<String, String> updatedParams = DepartmentTestData.getUpdatedParams();
        updatedParams.set(ID, NOT_FOUND + "");
        perform(MockMvcRequestBuilders.post(DEPARTMENTS_UPDATE_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(exception().exceptionPage(ENTITY_NOT_FOUND, NotFoundException.class));
    }

    @Test
    void updateUnAuthorize() throws Exception {
        perform(MockMvcRequestBuilders.post(DEPARTMENTS_UPDATE_URL)
                .params(DepartmentTestData.getUpdatedParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertNotEquals(service.get(DEPARTMENT1_ID).getName(), DepartmentTestData.getUpdated().getName());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateForbidden() throws Exception {
        perform(MockMvcRequestBuilders.post(DEPARTMENTS_UPDATE_URL)
                .params(DepartmentTestData.getUpdatedParams())
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertNotEquals(service.get(DEPARTMENT1_ID).getName(), DepartmentTestData.getUpdated().getName());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void updateInvalid() throws Exception {
        MultiValueMap<String, String> updatedInvalidParams = DepartmentTestData.getUpdatedInvalidParams();
        perform(MockMvcRequestBuilders.post(DEPARTMENTS_UPDATE_URL)
                .params(updatedInvalidParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(DEPARTMENT_ATTRIBUTE, NAME_PARAM,
                        BOSS_LAST_NAME_PARAM, BOSS_FIRST_NAME_PARAM, BOSS_MIDDLE_NAME_PARAM, BOSS_PHONE_PARAM))
                .andExpect(view().name(DEPARTMENTS_FORM_VIEW));
        assertNotEquals(service.get(DEPARTMENT1_ID).getName(), updatedInvalidParams.get(NAME_PARAM).get(0));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void updateDuplicateName() throws Exception {
        MultiValueMap<String, String> updatedParams = DepartmentTestData.getUpdatedParams();
        updatedParams.set(NAME_PARAM, DEPARTMENT2_NAME);
        perform(MockMvcRequestBuilders.post(DEPARTMENTS_UPDATE_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(DEPARTMENT_ATTRIBUTE, NAME_PARAM, DUPLICATE_ERROR_CODE))
                .andExpect(view().name(DEPARTMENTS_FORM_VIEW));
        assertNotEquals(service.get(DEPARTMENT1_ID).getName(), DEPARTMENT2_NAME);
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void delete() throws Exception {
        perform(MockMvcRequestBuilders.post(DEPARTMENTS_DELETE_URL + DEPARTMENT1_ID)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(DEPARTMENTS_URL))
                .andExpect(flash().attribute(ACTION, "Department " + DEPARTMENT1_NAME + " was deleted"));
        entityManager.clear();
        assertThrows(NotFoundException.class, () -> service.get(DEPARTMENT1_ID));
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