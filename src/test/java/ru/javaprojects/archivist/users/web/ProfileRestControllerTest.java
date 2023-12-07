package ru.javaprojects.archivist.users.web;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaprojects.archivist.AbstractControllerTest;
import ru.javaprojects.archivist.common.error.NotFoundException;
import ru.javaprojects.archivist.users.UserService;
import ru.javaprojects.archivist.users.UserTestData;
import ru.javaprojects.archivist.users.password_reset.PasswordResetService;
import ru.javaprojects.archivist.users.password_reset.PasswordResetToken;
import ru.javaprojects.archivist.users.password_reset.mail.MailSender;

import java.util.Date;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.javaprojects.archivist.CommonTestData.ADMIN_MAIL;
import static ru.javaprojects.archivist.CommonTestData.USER_MAIL;
import static ru.javaprojects.archivist.common.config.SecurityConfig.PASSWORD_ENCODER;
import static ru.javaprojects.archivist.common.web.PathUIController.LOGIN_URL;
import static ru.javaprojects.archivist.users.UserTestData.*;
import static ru.javaprojects.archivist.users.password_reset.PasswordResetService.*;
import static ru.javaprojects.archivist.users.web.ProfileUIController.PROFILE_URL;

public class ProfileRestControllerTest extends AbstractControllerTest {
    private static final String PROFILE_CHANGE_PASSWORD_URL = PROFILE_URL + "/" + PASSWORD;
    private static final String PROFILE_FORGOT_PASSWORD_URL = PROFILE_URL + "/forgotPassword";

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordResetService passwordResetService;

    @MockBean
    private MailSender mailSender;

    @Value("${password-reset.url}")
    private String passwordResetUrl;

    @Test
    @WithUserDetails(USER_MAIL)
    void changePassword() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROFILE_CHANGE_PASSWORD_URL)
                .param(PASSWORD, NEW_PASSWORD)
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertTrue(PASSWORD_ENCODER.matches(NEW_PASSWORD, userService.get(UserTestData.USER_ID).getPassword()));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void changePasswordInvalid() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROFILE_CHANGE_PASSWORD_URL)
                .param(PASSWORD, INVALID_PASSWORD)
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        ConstraintViolationException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail(UserTestData.CHANGE_PASSWORD_LENGTH_ERROR))
                .andExpect(problemInstance(PROFILE_CHANGE_PASSWORD_URL));
        assertFalse(PASSWORD_ENCODER.matches(INVALID_PASSWORD, userService.get(UserTestData.USER_ID).getPassword()));
    }

    @Test
    void changePasswordUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.patch(PROFILE_CHANGE_PASSWORD_URL)
                .param(PASSWORD, NEW_PASSWORD)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertFalse(PASSWORD_ENCODER.matches(NEW_PASSWORD, userService.get(UserTestData.USER_ID).getPassword()));
    }

    @Test
    void forgotPasswordNewTokenCreated() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_FORGOT_PASSWORD_URL)
                .param(EMAIL, USER_MAIL)
                .with(csrf()))
                .andExpect(status().isNoContent());
        PasswordResetToken passwordResetToken = passwordResetService.getByEmail(USER_MAIL);
        assertTrue(passwordResetToken.getExpiryDate().after(new Date()));
        String text = PASSWORD_RESET_MESSAGE_TEXT_TEMPLATE +
                String.format(PASSWORD_RESET_MESSAGE_LINK_TEMPLATE, passwordResetUrl, passwordResetToken.getToken());
        Mockito.verify(mailSender, Mockito.times(1)).sendEmail(USER_MAIL, PASSWORD_RESET_MESSAGE_SUBJECT, text);
    }

    @Test
    void forgotPasswordExistingTokenUpdated() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_FORGOT_PASSWORD_URL)
                .param(EMAIL, ADMIN_MAIL)
                .with(csrf()))
                .andExpect(status().isNoContent());
        PasswordResetToken passwordResetToken = passwordResetService.getByEmail(ADMIN_MAIL);
        assertTrue(passwordResetToken.getExpiryDate().after(new Date()));
        assertNotEquals(passwordResetToken.getExpiryDate(), adminPasswordResetToken.getExpiryDate());
        assertNotEquals(passwordResetToken.getToken(), adminPasswordResetToken.getToken());
        String text = PASSWORD_RESET_MESSAGE_TEXT_TEMPLATE +
                String.format(PASSWORD_RESET_MESSAGE_LINK_TEMPLATE, passwordResetUrl, passwordResetToken.getToken());
        Mockito.verify(mailSender, Mockito.times(1)).sendEmail(ADMIN_MAIL, PASSWORD_RESET_MESSAGE_SUBJECT, text);
    }

    @Test
    void forgotPasswordNotFound() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_FORGOT_PASSWORD_URL)
                .param(EMAIL, NOT_EXISTING_EMAIL)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail("Not found user with email=" + NOT_EXISTING_EMAIL))
                .andExpect(problemInstance(PROFILE_FORGOT_PASSWORD_URL));
        assertThrows(NotFoundException.class, () -> passwordResetService.getByEmail(NOT_EXISTING_EMAIL));
        Mockito.verify(mailSender, Mockito.times(0)).sendEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void forgotPasswordAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.post(PROFILE_FORGOT_PASSWORD_URL)
                .param(EMAIL, USER_MAIL)
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertThrows(NotFoundException.class, () -> passwordResetService.getByEmail(USER_MAIL));
        Mockito.verify(mailSender, Mockito.times(0)).sendEmail(Mockito.anyString(), Mockito.anyString(), Mockito.anyString());
    }
}
