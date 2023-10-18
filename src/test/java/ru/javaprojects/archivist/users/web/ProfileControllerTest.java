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
import ru.javaprojects.archivist.users.User;
import ru.javaprojects.archivist.users.UserService;
import ru.javaprojects.archivist.users.UserTestData;
import ru.javaprojects.archivist.users.password_reset.*;
import ru.javaprojects.archivist.users.password_reset.mail.MailSender;

import java.util.Date;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.archivist.AbstractControllerTest.ExceptionResultMatchers.exception;
import static ru.javaprojects.archivist.CommonTestData.*;
import static ru.javaprojects.archivist.common.config.SecurityConfig.PASSWORD_ENCODER;
import static ru.javaprojects.archivist.users.UserTestData.*;
import static ru.javaprojects.archivist.users.password_reset.PasswordResetService.*;
import static ru.javaprojects.archivist.users.web.LoginController.LOGIN_URL;
import static ru.javaprojects.archivist.users.web.ProfileUIController.PROFILE_URL;

class ProfileControllerTest extends AbstractControllerTest {
    private static final String PROFILE_CHANGE_PASSWORD_URL = PROFILE_URL + "/" + PASSWORD;
    private static final String PROFILE_FORGOT_PASSWORD_URL = PROFILE_URL + "/forgotPassword";
    private static final String PROFILE_RESET_PASSWORD_URL = PROFILE_URL + "/resetPassword";

    public static final String RESET_PASSWORD_VIEW = "users/reset-password";

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordResetService passwordResetService;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @MockBean
    private MailSender mailSender;

    @Value("${password-reset.url}")
    private String passwordResetUrl;

    @Test
    @WithUserDetails(USER_MAIL)
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
        String text = PASSWORD_RESET_MESSAGE_TEXT_TEMPLATE + String.format(PASSWORD_RESET_MESSAGE_LINK_TEMPLATE, passwordResetUrl, passwordResetToken.getToken());
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
        String text = PASSWORD_RESET_MESSAGE_TEXT_TEMPLATE + String.format(PASSWORD_RESET_MESSAGE_LINK_TEMPLATE, passwordResetUrl, passwordResetToken.getToken());
        Mockito.verify(mailSender, Mockito.times(1)).sendEmail(ADMIN_MAIL, PASSWORD_RESET_MESSAGE_SUBJECT, text);
    }

    @Test
    void forgotPasswordNotFound() throws Exception {
        String notExistingEmail = "notExisted@gmail.com";
        perform(MockMvcRequestBuilders.post(PROFILE_FORGOT_PASSWORD_URL)
                .param(EMAIL, notExistingEmail)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail("Not found user with email=" + notExistingEmail))
                .andExpect(problemInstance(PROFILE_FORGOT_PASSWORD_URL));
        assertThrows(NotFoundException.class, () -> passwordResetService.getByEmail(notExistingEmail));
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
                        PASSWORD_RESET_TO_MATCHER.assertMatch((PasswordResetTo) Objects.requireNonNull(result.getModelAndView()).getModel().get("passwordResetTo"), new PasswordResetTo(token)));
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
        assertTrue(PASSWORD_ENCODER.matches(NEW_PASSWORD, userService.get(ADMIN_ID).getPassword()));
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
        assertFalse(PASSWORD_ENCODER.matches(INVALID_PASSWORD, userService.get(ADMIN_ID).getPassword()));
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
