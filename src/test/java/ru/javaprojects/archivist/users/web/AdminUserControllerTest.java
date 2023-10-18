package ru.javaprojects.archivist.users.web;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.MultiValueMap;
import ru.javaprojects.archivist.AbstractControllerTest;
import ru.javaprojects.archivist.common.error.NotFoundException;
import ru.javaprojects.archivist.users.User;
import ru.javaprojects.archivist.users.UserService;
import ru.javaprojects.archivist.users.UserTo;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.archivist.AbstractControllerTest.ExceptionResultMatchers.exception;
import static ru.javaprojects.archivist.CommonTestData.*;
import static ru.javaprojects.archivist.common.config.SecurityConfig.PASSWORD_ENCODER;
import static ru.javaprojects.archivist.common.util.validation.Constants.DUPLICATE_ERROR_CODE;
import static ru.javaprojects.archivist.users.UserTestData.*;
import static ru.javaprojects.archivist.users.UserUtil.asTo;
import static ru.javaprojects.archivist.users.web.AdminUserUIController.USERS_URL;
import static ru.javaprojects.archivist.users.web.LoginController.LOGIN_URL;

public class AdminUserControllerTest extends AbstractControllerTest {
    private static final String USERS_ADD_FORM_URL = USERS_URL + "/add";
    private static final String USERS_CREATE_URL = USERS_URL + "/create";
    private static final String USERS_EDIT_FORM_URL = USERS_URL + "/edit/";
    private static final String USERS_UPDATE_URL = USERS_URL + "/update";
    private static final String USERS_URL_SLASH = USERS_URL + "/";
    private static final String USERS_CHANGE_PASSWORD_URL = USERS_URL + "/password/";

    private static final String USERS_VIEW = "users/users";
    private static final String USER_ADD_VIEW = "users/user-add";
    private static final String USER_EDIT_VIEW = "users/user-edit";

    @Autowired
    private UserService service;

    @Test
    @WithUserDetails(ADMIN_MAIL)
    @SuppressWarnings("unchecked")
    void getAll() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(USERS_URL)
                .params(getPageableParams()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(USERS_ATTRIBUTE))
                .andExpect(view().name(USERS_VIEW));
        Page<User> users = (Page<User>) Objects.requireNonNull(actions.andReturn().getModelAndView())
                .getModel().get(USERS_ATTRIBUTE);
        assertEquals(4, users.getTotalElements());
        assertEquals(2, users.getTotalPages());
        USER_MATCHER.assertMatch(users.getContent(), List.of(user, admin));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    @SuppressWarnings("unchecked")
    void getAllByKeyword() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(USERS_URL)
                .param(KEYWORD, "jack"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(USERS_ATTRIBUTE))
                .andExpect(view().name(USERS_VIEW));
        Page<User> users = (Page<User>) Objects.requireNonNull(actions.andReturn().getModelAndView())
                .getModel().get(USERS_ATTRIBUTE);
        assertEquals(1, users.getTotalElements());
        assertEquals(1, users.getTotalPages());
        USER_MATCHER.assertMatch(users.getContent(), List.of(admin));
    }

    @Test
    void getAllUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(USERS_URL)
                .params(getPageableParams()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void getAllForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(USERS_URL)
                .params(getPageableParams()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void showAddForm() throws Exception {
        perform(MockMvcRequestBuilders.get(USERS_ADD_FORM_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(USER_ATTRIBUTE))
                .andExpect(view().name(USER_ADD_VIEW))
                .andExpect(result ->
                        USER_MATCHER.assertMatch((User) Objects.requireNonNull(result.getModelAndView()).getModel().get(USER_ATTRIBUTE), new User()));
    }

    @Test
    void showAddFormUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(USERS_ADD_FORM_URL))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showAddFormForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(USERS_ADD_FORM_URL))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void create() throws Exception {
        User newUser = getNew();
        perform(MockMvcRequestBuilders.post(USERS_CREATE_URL)
                .params(getNewParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(USERS_URL))
                .andExpect(flash().attribute(ACTION, "User " + newUser.getFullName() + " was created"));
        User created = service.getByEmail(newUser.getEmail());
        newUser.setId(created.id());
        USER_MATCHER.assertMatch(created, newUser);
    }

    @Test
    void createUnAuthorize() throws Exception {
        perform(MockMvcRequestBuilders.post(USERS_CREATE_URL)
                .params(getNewParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertThrows(NotFoundException.class, () -> service.getByEmail(getNew().getEmail()));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createForbidden() throws Exception {
        perform(MockMvcRequestBuilders.post(USERS_CREATE_URL)
                .params(getNewParams())
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertThrows(NotFoundException.class, () -> service.getByEmail(getNew().getEmail()));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void createInvalid() throws Exception {
        MultiValueMap<String, String> newInvalidParams = getNewInvalidParams();
        perform(MockMvcRequestBuilders.post(USERS_CREATE_URL)
                .params(newInvalidParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(USER_ATTRIBUTE, FIRST_NAME, LAST_NAME, PASSWORD, EMAIL, ROLES))
                .andExpect(view().name(USER_ADD_VIEW));
        assertThrows(NotFoundException.class, () -> service.getByEmail(newInvalidParams.get(EMAIL).get(0)));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void createDuplicateEmail() throws Exception {
        MultiValueMap<String, String> newParams = getNewParams();
        newParams.set(EMAIL, USER_MAIL);
        perform(MockMvcRequestBuilders.post(USERS_CREATE_URL)
                .params(newParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(USER_ATTRIBUTE, EMAIL, DUPLICATE_ERROR_CODE))
                .andExpect(view().name(USER_ADD_VIEW));
        assertNotEquals(getNew().getFullName(), service.getByEmail(USER_MAIL).getFullName());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void showEditForm() throws Exception {
        perform(MockMvcRequestBuilders.get(USERS_EDIT_FORM_URL + USER_ID))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(USER_TO_ATTRIBUTE))
                .andExpect(view().name(USER_EDIT_VIEW))
                .andExpect(result ->
                        USER_TO_MATCHER.assertMatch((UserTo)Objects.requireNonNull(result.getModelAndView()).getModel().get(USER_TO_ATTRIBUTE), asTo(user)));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void showEditFormNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(USERS_EDIT_FORM_URL + NOT_FOUND))
                .andExpect(exception().exceptionPage(ENTITY_NOT_FOUND, NotFoundException.class));
    }

    @Test
    void showEditFormUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(USERS_EDIT_FORM_URL + USER_ID))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showEditFormForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(USERS_EDIT_FORM_URL + USER_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void update() throws Exception {
        User updatedUser = getUpdated();
        perform(MockMvcRequestBuilders.post(USERS_UPDATE_URL)
                .params(getUpdatedParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(USERS_URL))
                .andExpect(flash().attribute(ACTION, "User " + updatedUser.getFullName() + " was updated"));
        USER_MATCHER.assertMatch(service.get(USER_ID), updatedUser);
    }

    //Check UniqueMailValidator works correct when update
    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateEmailNotChange() throws Exception {
        User updatedUser = getUpdated();
        updatedUser.setEmail(USER_MAIL);
        MultiValueMap<String, String> updatedParams = getUpdatedParams();
        updatedParams.set(EMAIL, USER_MAIL);
        perform(MockMvcRequestBuilders.post(USERS_UPDATE_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(USERS_URL))
                .andExpect(flash().attribute(ACTION, "User " + updatedUser.getFullName() + " was updated"));
        USER_MATCHER.assertMatch(service.get(USER_ID), updatedUser);
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateNotFound() throws Exception {
        MultiValueMap<String, String> updatedParams = getUpdatedParams();
        updatedParams.set(ID, NOT_FOUND + "");
        perform(MockMvcRequestBuilders.post(USERS_UPDATE_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(exception().exceptionPage(ENTITY_NOT_FOUND, NotFoundException.class));
    }

    @Test
    void updateUnAuthorize() throws Exception {
        perform(MockMvcRequestBuilders.post(USERS_UPDATE_URL)
                .params(getUpdatedParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertNotEquals(service.get(USER_ID).getEmail(), getUpdated().getEmail());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateForbidden() throws Exception {
        perform(MockMvcRequestBuilders.post(USERS_UPDATE_URL)
                .params(getUpdatedParams())
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertNotEquals(service.get(USER_ID).getEmail(), getUpdated().getEmail());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateInvalid() throws Exception {
        MultiValueMap<String, String> updatedInvalidParams = getUpdatedInvalidParams();
        perform(MockMvcRequestBuilders.post(USERS_UPDATE_URL)
                .params(updatedInvalidParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(USER_TO_ATTRIBUTE, FIRST_NAME, LAST_NAME, EMAIL, ROLES))
                .andExpect(view().name(USER_EDIT_VIEW));
        assertNotEquals(service.get(USER_ID).getEmail(), updatedInvalidParams.get(EMAIL).get(0));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateDuplicateEmail() throws Exception {
        MultiValueMap<String, String> updatedParams = getUpdatedParams();
        updatedParams.set(EMAIL, ADMIN_MAIL);
        perform(MockMvcRequestBuilders.post(USERS_UPDATE_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(USER_TO_ATTRIBUTE, EMAIL, DUPLICATE_ERROR_CODE))
                .andExpect(view().name(USER_EDIT_VIEW));
        assertNotEquals(service.get(USER_ID).getEmail(), ADMIN_MAIL);
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void enable() throws Exception {
        perform(MockMvcRequestBuilders.patch(USERS_URL_SLASH + USER_ID)
                .param(ENABLED, FALSE)
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertFalse(service.get(USER_ID).isEnabled());

        perform(MockMvcRequestBuilders.patch(USERS_URL_SLASH + USER_ID)
                .param(ENABLED, TRUE)
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertTrue(service.get(USER_ID).isEnabled());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void enableNotFound() throws Exception {
        perform(MockMvcRequestBuilders.patch(USERS_URL_SLASH + NOT_FOUND)
                .param(ENABLED, FALSE)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(USERS_URL_SLASH + NOT_FOUND));
    }

    @Test
    void enableUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.patch(USERS_URL_SLASH + USER_ID)
                .param(ENABLED, FALSE)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertTrue(service.get(USER_ID).isEnabled());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void enableForbidden() throws Exception {
        perform(MockMvcRequestBuilders.patch(USERS_URL_SLASH + USER_ID)
                .param(ENABLED, FALSE)
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertTrue(service.get(USER_ID).isEnabled());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void delete() throws Exception {
        perform(MockMvcRequestBuilders.delete(USERS_URL_SLASH + USER_ID)
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertThrows(NotFoundException.class, () -> service.get(USER_ID));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void deleteNotFound() throws Exception {
        perform(MockMvcRequestBuilders.delete(USERS_URL_SLASH + NOT_FOUND)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(USERS_URL_SLASH + NOT_FOUND));
    }

    @Test
    void deleteUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.delete(USERS_URL_SLASH + USER_ID)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertDoesNotThrow(() -> service.get(USER_ID));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void deleteForbidden() throws Exception {
        perform(MockMvcRequestBuilders.delete(USERS_URL_SLASH + USER_ID)
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertDoesNotThrow(() -> service.get(USER_ID));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void changePassword() throws Exception {
        perform(MockMvcRequestBuilders.patch(USERS_CHANGE_PASSWORD_URL + USER_ID)
                .param(PASSWORD, NEW_PASSWORD)
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertTrue(PASSWORD_ENCODER.matches(NEW_PASSWORD, service.get(USER_ID).getPassword()));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void changePasswordNotFound() throws Exception {
        perform(MockMvcRequestBuilders.patch(USERS_CHANGE_PASSWORD_URL + NOT_FOUND)
                .param(PASSWORD, NEW_PASSWORD)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(USERS_CHANGE_PASSWORD_URL + NOT_FOUND));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void changePasswordInvalid() throws Exception {
        perform(MockMvcRequestBuilders.patch(USERS_CHANGE_PASSWORD_URL + USER_ID)
                .param(PASSWORD, INVALID_PASSWORD)
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        ConstraintViolationException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail(CHANGE_PASSWORD_LENGTH_ERROR))
                .andExpect(problemInstance(USERS_CHANGE_PASSWORD_URL + USER_ID));
        assertFalse(PASSWORD_ENCODER.matches(INVALID_PASSWORD, service.get(USER_ID).getPassword()));
    }

    @Test
    void changePasswordUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.patch(USERS_CHANGE_PASSWORD_URL + USER_ID)
                .param(PASSWORD, NEW_PASSWORD)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertFalse(PASSWORD_ENCODER.matches(NEW_PASSWORD, service.get(USER_ID).getPassword()));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void changePasswordForbidden() throws Exception {
        perform(MockMvcRequestBuilders.patch(USERS_CHANGE_PASSWORD_URL + USER_ID)
                .param(PASSWORD, NEW_PASSWORD)
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertFalse(PASSWORD_ENCODER.matches(NEW_PASSWORD, service.get(USER_ID).getPassword()));
    }
}
