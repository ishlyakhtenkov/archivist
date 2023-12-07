package ru.javaprojects.archivist.users.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaprojects.archivist.AbstractControllerTest;
import ru.javaprojects.archivist.common.error.NotFoundException;
import ru.javaprojects.archivist.users.User;
import ru.javaprojects.archivist.users.UserService;
import ru.javaprojects.archivist.users.UserTestData;
import ru.javaprojects.archivist.users.password_reset.PasswordResetException;
import ru.javaprojects.archivist.users.password_reset.PasswordResetTo;
import ru.javaprojects.archivist.users.password_reset.PasswordResetTokenRepository;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.archivist.AbstractControllerTest.ExceptionResultMatchers.exception;
import static ru.javaprojects.archivist.CommonTestData.*;
import static ru.javaprojects.archivist.common.config.SecurityConfig.PASSWORD_ENCODER;
import static ru.javaprojects.archivist.common.web.PathUIController.LOGIN_URL;
import static ru.javaprojects.archivist.users.UserTestData.*;
import static ru.javaprojects.archivist.users.web.ProfileUIController.PROFILE_URL;

class ProfileUIControllerTest extends AbstractControllerTest {
    private static final String PROFILE_RESET_PASSWORD_URL = PROFILE_URL + "/resetPassword";
    private static final String PROFILE_VIEW = "users/profile";
    private static final String RESET_PASSWORD_VIEW = "users/reset-password";

    @Autowired
    private UserService service;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Test
    @WithUserDetails(USER_MAIL)
    void showProfilePage() throws Exception {
        perform(MockMvcRequestBuilders.get(PROFILE_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(PROFILE_VIEW))
                .andExpect(model().attributeExists(USER_ATTRIBUTE))
                .andExpect(result -> USER_MATCHER.assertMatch((User)Objects.requireNonNull(result.getModelAndView())
                        .getModel().get(USER_ATTRIBUTE), user));
    }

    @Test
    void showProfilePageUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(PROFILE_URL))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }



    @Test
    void showResetPasswordForm() throws Exception {
        String token = adminPasswordResetToken.getToken();
        perform(MockMvcRequestBuilders.get(PROFILE_RESET_PASSWORD_URL)
                .param(TOKEN, token)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(PASSWORD_RESET_TO_ATTRIBUTE))
                .andExpect(view().name(RESET_PASSWORD_VIEW))
                .andExpect(result ->
                        PASSWORD_RESET_TO_MATCHER.assertMatch((PasswordResetTo) Objects.requireNonNull(result.getModelAndView())
                                .getModel().get(PASSWORD_RESET_TO_ATTRIBUTE), new PasswordResetTo(token)));
    }

    @Test
    void showResetPasswordFormTokenNotExist() throws Exception {
        perform(MockMvcRequestBuilders.get(PROFILE_RESET_PASSWORD_URL)
                .param(TOKEN, NOT_EXISTING_TOKEN)
                .with(csrf()))
                .andExpect(exception().exceptionPage(PASSWORD_RESET_TOKEN_NOT_FOUND, NotFoundException.class));
    }

    @Test
    void showResetPasswordFormTokenExpired() throws Exception {
        perform(MockMvcRequestBuilders.get(PROFILE_RESET_PASSWORD_URL)
                .param(TOKEN, EXPIRED_TOKEN)
                .with(csrf()))
                .andExpect(exception().exceptionPage(PASSWORD_RESET_TOKEN_NOT_EXPIRED, PasswordResetException.class));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void showResetPasswordFormAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(PROFILE_RESET_PASSWORD_URL)
                .param(TOKEN, ADMIN_TOKEN)
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void resetPassword() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_RESET_PASSWORD_URL)
                .param(TOKEN, ADMIN_TOKEN)
                .param(PASSWORD, NEW_PASSWORD)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(LOGIN_URL))
                .andExpect(flash().attribute(ACTION, "Password has been successfully reset"));
        assertTrue(PASSWORD_ENCODER.matches(NEW_PASSWORD, service.get(ADMIN_ID).getPassword()));
        assertTrue(passwordResetTokenRepository.findByToken(ADMIN_TOKEN).isEmpty());
    }

    @Test
    void resetPasswordTokenNotExist() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_RESET_PASSWORD_URL)
                .param(TOKEN, NOT_EXISTING_TOKEN)
                .param(PASSWORD, NEW_PASSWORD)
                .with(csrf()))
                .andExpect(exception().exceptionPage(PASSWORD_RESET_TOKEN_NOT_FOUND, NotFoundException.class));
    }

    @Test
    void resetPasswordTokenExpired() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_RESET_PASSWORD_URL)
                .param(TOKEN, EXPIRED_TOKEN)
                .param(PASSWORD, NEW_PASSWORD)
                .with(csrf()))
                .andExpect(exception().exceptionPage(PASSWORD_RESET_TOKEN_NOT_EXPIRED, PasswordResetException.class));
    }

    @Test
    void resetPasswordInvalid() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_RESET_PASSWORD_URL)
                .param(TOKEN, ADMIN_TOKEN)
                .param(PASSWORD, INVALID_PASSWORD)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(PASSWORD_RESET_TO_ATTRIBUTE, PASSWORD))
                .andExpect(view().name(RESET_PASSWORD_VIEW));
        assertFalse(PASSWORD_ENCODER.matches(INVALID_PASSWORD, service.get(ADMIN_ID).getPassword()));
    }

    @Test
    void resetPasswordWithoutTokenParam() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_RESET_PASSWORD_URL)
                .param(PASSWORD, NEW_PASSWORD)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(PASSWORD_RESET_TO_ATTRIBUTE, TOKEN))
                .andExpect(view().name(RESET_PASSWORD_VIEW));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void resetPasswordAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(PROFILE_RESET_PASSWORD_URL)
                .param(TOKEN, ADMIN_TOKEN)
                .param(PASSWORD, NEW_PASSWORD)
                .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
