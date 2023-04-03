package ru.javaprojects.archivist.web;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaprojects.archivist.AbstractControllerTest;
import ru.javaprojects.archivist.service.UserService;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static ru.javaprojects.archivist.config.SecurityConfig.PASSWORD_ENCODER;
import static ru.javaprojects.archivist.web.ProfileUIController.PROFILE_URL;
import static ru.javaprojects.archivist.web.UserTestData.*;

class ProfileControllerTest extends AbstractControllerTest {

    @Autowired
    private UserService service;

    @Test
    @WithUserDetails(USER_MAIL)
    void showProfilePage() throws Exception {
        perform(MockMvcRequestBuilders.get(PROFILE_URL))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(USER_MATCHER.modelContent(user, "user"));
    }

    @Test
    void showProfilePageWhenUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(PROFILE_URL))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith("/login")));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void changePassword() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROFILE_URL + "/password")
                .param("password", "newPassword")
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertTrue(PASSWORD_ENCODER.matches("newPassword", service.get(USER_ID).getPassword()));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void changePasswordInvalid() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROFILE_URL + "/password")
                .param("password", "pass")
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        ConstraintViolationException.class))
                .andExpect(problemDetailTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemDetailMessage("changePassword.password: size must be between 5 and 32"));
        assertFalse(PASSWORD_ENCODER.matches("pass", service.get(USER_ID).getPassword()));
    }

    @Test
    void changePasswordWhenUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROFILE_URL + "/password")
                .param("password", "newPassword")
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith("/login")));
    }
}
