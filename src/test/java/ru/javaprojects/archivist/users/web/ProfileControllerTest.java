package ru.javaprojects.archivist.users.web;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaprojects.archivist.AbstractControllerTest;
import ru.javaprojects.archivist.users.User;
import ru.javaprojects.archivist.users.UserService;
import ru.javaprojects.archivist.users.UserTestData;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.archivist.common.config.SecurityConfig.PASSWORD_ENCODER;
import static ru.javaprojects.archivist.users.web.LoginController.LOGIN_URL;
import static ru.javaprojects.archivist.users.web.ProfileUIController.PROFILE_URL;
import static ru.javaprojects.archivist.users.UserTestData.USER_ATTRIBUTE;

class ProfileControllerTest extends AbstractControllerTest {
    private static final String PROFILE_CHANGE_PASSWORD_URL = PROFILE_URL + "/" + UserTestData.PASSWORD;

    @Autowired
    private UserService service;

    @Test
    @WithUserDetails(UserTestData.USER_MAIL)
    void showProfilePage() throws Exception {
        perform(MockMvcRequestBuilders.get(PROFILE_URL))
                .andExpect(status().isOk())
                .andExpect(view().name("users/profile"))
                .andExpect(model().attributeExists(USER_ATTRIBUTE))
                .andExpect(result ->
                        UserTestData.USER_MATCHER.assertMatch((User)Objects.requireNonNull(
                                result.getModelAndView()).getModel().get(USER_ATTRIBUTE), UserTestData.user));
    }

    @Test
    void showProfilePageUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(PROFILE_URL))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(UserTestData.USER_MAIL)
    void changePassword() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROFILE_CHANGE_PASSWORD_URL)
                .param(UserTestData.PASSWORD, UserTestData.NEW_PASSWORD)
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertTrue(PASSWORD_ENCODER.matches(UserTestData.NEW_PASSWORD, service.get(UserTestData.USER_ID).getPassword()));
    }

    @Test
    @WithUserDetails(UserTestData.USER_MAIL)
    void changePasswordInvalid() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROFILE_CHANGE_PASSWORD_URL)
                .param(UserTestData.PASSWORD, UserTestData.INVALID_PASSWORD)
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        ConstraintViolationException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail(UserTestData.CHANGE_PASSWORD_LENGTH_ERROR))
                .andExpect(problemInstance(PROFILE_CHANGE_PASSWORD_URL));
        assertFalse(PASSWORD_ENCODER.matches(UserTestData.INVALID_PASSWORD, service.get(UserTestData.USER_ID).getPassword()));
    }

    @Test
    void changePasswordUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROFILE_CHANGE_PASSWORD_URL)
                .param(UserTestData.PASSWORD, UserTestData.NEW_PASSWORD)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertFalse(PASSWORD_ENCODER.matches(UserTestData.NEW_PASSWORD, service.get(UserTestData.USER_ID).getPassword()));
    }
}
