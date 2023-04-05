package ru.javaprojects.archivist.web;

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
import ru.javaprojects.archivist.model.User;
import ru.javaprojects.archivist.service.UserService;
import ru.javaprojects.archivist.to.UserTo;
import ru.javaprojects.archivist.util.exception.NotFoundException;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.archivist.config.SecurityConfig.PASSWORD_ENCODER;
import static ru.javaprojects.archivist.util.UserUtil.asTo;
import static ru.javaprojects.archivist.web.AdminUserUIController.USERS_URL;
import static ru.javaprojects.archivist.web.ProfileUIController.PROFILE_URL;
import static ru.javaprojects.archivist.web.UserTestData.*;

public class AdminUserControllerTest extends AbstractControllerTest {

    @Autowired
    private UserService service;

    @Test
    @WithUserDetails(ADMIN_MAIL)
    @SuppressWarnings("unchecked")
    void getAll() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(USERS_URL)
                .param("page", "0")
                .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("usersPage"))
                .andExpect(view().name("users"));
        Page<User> usersPage = (Page<User>) Objects.requireNonNull(actions.andReturn().getModelAndView())
                .getModel().get("usersPage");
        assertEquals(4, usersPage.getTotalElements());
        assertEquals(2, usersPage.getTotalPages());
        USER_MATCHER.assertMatch(usersPage.getContent(), List.of(user, admin));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    @SuppressWarnings("unchecked")
    void getAllByKeyword() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(USERS_URL)
                .param("keyword", "jack"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("usersPage"))
                .andExpect(view().name("users"));
        Page<User> usersPage = (Page<User>) Objects.requireNonNull(actions.andReturn().getModelAndView())
                .getModel().get("usersPage");
        assertEquals(1, usersPage.getTotalElements());
        assertEquals(1, usersPage.getTotalPages());
        USER_MATCHER.assertMatch(usersPage.getContent(), List.of(admin));
    }

    @Test
    void getUsersUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(USERS_URL)
                .param("page", "0")
                .param("size", "2"))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith("/login")));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void getUsersForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(USERS_URL)
                .param("page", "0")
                .param("size", "2"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void showAddForm() throws Exception {
        perform(MockMvcRequestBuilders.get(USERS_URL + "/add"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("user"))
                .andExpect(view().name("user-add"))
                .andExpect(result ->
                        USER_MATCHER.assertMatch((User) Objects.requireNonNull(result.getModelAndView()).getModel().get("user"), new User()));
    }

    @Test
    void showAddFormUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(USERS_URL + "/add"))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith("/login")));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showAddFormForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(USERS_URL + "/add"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void create() throws Exception {
        User newUser = getNew();
        perform(MockMvcRequestBuilders.post(USERS_URL + "/create")
                .params(getNewParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/users"))
                .andExpect(flash().attribute("userCreated", newUser.getFullName()));
        User created = service.getByEmail(newUser.getEmail());
        newUser.setId(created.id());
        USER_MATCHER.assertMatch(created, newUser);
    }

    @Test
    void createUnAuthorize() throws Exception {
        perform(MockMvcRequestBuilders.post(USERS_URL + "/create")
                .params(getNewParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith("/login")));
        assertThrows(NotFoundException.class, () -> service.getByEmail(getNew().getEmail()));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createForbidden() throws Exception {
        perform(MockMvcRequestBuilders.post(USERS_URL + "/create")
                .params(getNewParams())
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertThrows(NotFoundException.class, () -> service.getByEmail(getNew().getEmail()));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void createInvalid() throws Exception {
        MultiValueMap<String, String> newInvalidParams = getNewInvalidParams();
        perform(MockMvcRequestBuilders.post(USERS_URL + "/create")
                .params(newInvalidParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("user", "firstName", "lastName", "password", "email", "roles"))
                .andExpect(view().name("user-add"));
        assertThrows(NotFoundException.class, () -> service.getByEmail(newInvalidParams.get("email").get(0)));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void createDuplicateEmail() throws Exception {
        MultiValueMap<String, String> newParams = getNewParams();
        newParams.set("email", USER_MAIL);
        perform(MockMvcRequestBuilders.post(USERS_URL + "/create")
                .params(newParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode("user", "email", "Duplicate"))
                .andExpect(view().name("user-add"));
        assertNotEquals(getNew().getFullName(), service.getByEmail(USER_MAIL).getFullName());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void showEditForm() throws Exception {
        perform(MockMvcRequestBuilders.get(USERS_URL + "/edit/" + USER_ID))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("userTo"))
                .andExpect(view().name("user-edit"))
                .andExpect(result ->
                        USER_TO_MATCHER.assertMatch((UserTo)Objects.requireNonNull(result.getModelAndView()).getModel().get("userTo"), asTo(user)));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void showEditFormNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(USERS_URL + "/edit/" + NOT_FOUND))
                .andExpect(status().isInternalServerError())
                .andExpect(model().attribute("typeMessage", "Internal Server Error"))
                .andExpect(model().attributeExists("exception"))
                .andExpect(model().attribute("message", "Entity with id=" + NOT_FOUND + " not found"))
                .andExpect(model().attribute("status", HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .andExpect(view().name("exception"))
                .andExpect(result ->
                        assertEquals(NotFoundException.class, Objects.requireNonNull(result.getResolvedException()).getClass()));
    }

    @Test
    void showEditFormUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(USERS_URL + "/edit/" + USER_ID))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith("/login")));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showEditFormForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(USERS_URL + "/edit/" + USER_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void update() throws Exception {
        User updatedUser = getUpdated();
        perform(MockMvcRequestBuilders.post(USERS_URL + "/update")
                .params(getUpdatedParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/users"))
                .andExpect(flash().attribute("userUpdated", updatedUser.getFullName()));
        USER_MATCHER.assertMatch(service.get(USER_ID), updatedUser);
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateEmailNotChange() throws Exception {
        User updatedUser = getUpdated();
        updatedUser.setEmail(USER_MAIL);
        MultiValueMap<String, String> updatedParams = getUpdatedParams();
        updatedParams.set("email", USER_MAIL);
        perform(MockMvcRequestBuilders.post(USERS_URL + "/update")
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/users"))
                .andExpect(flash().attribute("userUpdated", updatedUser.getFullName()));
        USER_MATCHER.assertMatch(service.get(USER_ID), updatedUser);
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateNotFound() throws Exception {
        MultiValueMap<String, String> updatedParams = getUpdatedParams();
        updatedParams.set("id", NOT_FOUND + "");
        perform(MockMvcRequestBuilders.post(USERS_URL + "/update")
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isInternalServerError())
                .andExpect(model().attribute("typeMessage", "Internal Server Error"))
                .andExpect(model().attributeExists("exception"))
                .andExpect(model().attribute("message", "Entity with id=" + NOT_FOUND + " not found"))
                .andExpect(model().attribute("status", HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .andExpect(view().name("exception"))
                .andExpect(result ->
                        assertEquals(NotFoundException.class, Objects.requireNonNull(result.getResolvedException()).getClass()));
    }

    @Test
    void updateUnAuthorize() throws Exception {
        perform(MockMvcRequestBuilders.post(USERS_URL + "/update")
                .params(getUpdatedParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith("/login")));
        assertNotEquals(service.get(USER_ID).getEmail(), getUpdated().getEmail());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateForbidden() throws Exception {
        perform(MockMvcRequestBuilders.post(USERS_URL + "/update")
                .params(getUpdatedParams())
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertNotEquals(service.get(USER_ID).getEmail(), getUpdated().getEmail());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateInvalid() throws Exception {
        MultiValueMap<String, String> updatedInvalidParams = getUpdatedInvalidParams();
        perform(MockMvcRequestBuilders.post(USERS_URL + "/update")
                .params(updatedInvalidParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("userTo", "firstName", "lastName", "email", "roles"))
                .andExpect(view().name("user-edit"));
        assertNotEquals(service.get(USER_ID).getEmail(), updatedInvalidParams.get("email").get(0));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateDuplicateEmail() throws Exception {
        MultiValueMap<String, String> updatedParams = getUpdatedParams();
        updatedParams.set("email", ADMIN_MAIL);
        perform(MockMvcRequestBuilders.post(USERS_URL + "/update")
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode("userTo", "email", "Duplicate"))
                .andExpect(view().name("user-edit"));
        assertNotEquals(service.get(USER_ID).getEmail(), ADMIN_MAIL);
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void enable() throws Exception {
        perform(MockMvcRequestBuilders.patch(USERS_URL + "/" + USER_ID)
                .param("enabled", "false")
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertFalse(service.get(USER_ID).isEnabled());

        perform(MockMvcRequestBuilders.patch(USERS_URL + "/" + USER_ID)
                .param("enabled", "true")
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertTrue(service.get(USER_ID).isEnabled());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void enableNotFound() throws Exception {
        perform(MockMvcRequestBuilders.patch(USERS_URL + "/" + NOT_FOUND)
                .param("enabled", "false")
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail("Entity with id=" + NOT_FOUND + " not found"))
                .andExpect(problemInstance(USERS_URL + "/" + NOT_FOUND));
    }

    @Test
    void enableUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.patch(USERS_URL + "/" + USER_ID)
                .param("enabled", "false")
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith("/login")));
        assertTrue(service.get(USER_ID).isEnabled());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void enableForbidden() throws Exception {
        perform(MockMvcRequestBuilders.patch(USERS_URL + "/" + USER_ID)
                .param("enabled", "false")
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertTrue(service.get(USER_ID).isEnabled());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void delete() throws Exception {
        perform(MockMvcRequestBuilders.delete(USERS_URL + "/" + USER_ID)
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertThrows(NotFoundException.class, () -> service.get(USER_ID));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void deleteNotFound() throws Exception {
        perform(MockMvcRequestBuilders.delete(USERS_URL + "/" + NOT_FOUND)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail("Entity with id=" + NOT_FOUND + " not found"))
                .andExpect(problemInstance(USERS_URL + "/" + NOT_FOUND));
    }

    @Test
    void deleteUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.delete(USERS_URL + "/" + USER_ID)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith("/login")));
        assertDoesNotThrow(() -> service.get(USER_ID));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void deleteForbidden() throws Exception {
        perform(MockMvcRequestBuilders.delete(USERS_URL + "/" + USER_ID)
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertDoesNotThrow(() -> service.get(USER_ID));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void changePassword() throws Exception {
        perform(MockMvcRequestBuilders.patch(USERS_URL + "/" + USER_ID + "/password")
                .param("password", "newPassword")
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertTrue(PASSWORD_ENCODER.matches("newPassword", service.get(USER_ID).getPassword()));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void changePasswordNotFound() throws Exception {
        perform(MockMvcRequestBuilders.patch(USERS_URL + "/" + NOT_FOUND + "/password")
                .param("password", "newPassword")
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail("Entity with id=" + NOT_FOUND + " not found"))
                .andExpect(problemInstance(USERS_URL + "/" + NOT_FOUND + "/password"));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void changePasswordInvalid() throws Exception {
        perform(MockMvcRequestBuilders.patch(USERS_URL + "/" + USER_ID + "/password")
                .param("password", "pass")
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        ConstraintViolationException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail("changePassword.password: size must be between 5 and 32"))
                .andExpect(problemInstance(USERS_URL + "/" + USER_ID + "/password"));
        assertFalse(PASSWORD_ENCODER.matches("pass", service.get(USER_ID).getPassword()));
    }

    @Test
    void changePasswordUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.patch(USERS_URL + "/" + USER_ID + "/password")
                .param("password", "newPassword")
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith("/login")));
        assertFalse(PASSWORD_ENCODER.matches("newPassword", service.get(USER_ID).getPassword()));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void changePasswordForbidden() throws Exception {
        perform(MockMvcRequestBuilders.patch(USERS_URL + "/" + USER_ID + "/password")
                .param("password", "newPassword")
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertFalse(PASSWORD_ENCODER.matches("newPassword", service.get(USER_ID).getPassword()));
    }
}
