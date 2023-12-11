package ru.javaprojects.archivist.users.password_reset;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.javaprojects.archivist.common.error.NotFoundException;
import ru.javaprojects.archivist.users.UserService;
import ru.javaprojects.archivist.users.password_reset.mail.MailSender;

import java.util.Date;
import java.util.UUID;

import static ru.javaprojects.archivist.common.config.SecurityConfig.PASSWORD_ENCODER;

@Service
@RequiredArgsConstructor
public class PasswordResetService {
    public static final String PASSWORD_RESET_MESSAGE_SUBJECT = "Archivist: Password reset message";
    public static final String PASSWORD_RESET_MESSAGE_LINK_TEMPLATE = "<a href='%s?token=%s'>Password reset link</a>";
    public static final String PASSWORD_RESET_MESSAGE_TEXT_TEMPLATE = "<p>Please follow the link to reset your password:</p>";

    private final PasswordResetTokenRepository repository;
    private final UserService userService;
    private final MailSender mailSender;

    @Value("${password-reset.token-expiration-time}")
    private long expirationTime;

    @Value("${password-reset.url}")
    private String passwordResetUrl;

    @Transactional
    public void sendPasswordResetEmail(String email) {
        Assert.notNull(email, "email must not be null");
        PasswordResetToken passwordResetToken = repository.findByUserEmailIgnoreCase(email)
                .orElse(new PasswordResetToken(userService.getByEmail(email)));
        String token = UUID.randomUUID().toString();
        passwordResetToken.setToken(token);
        passwordResetToken.setExpiryDate(new Date(System.currentTimeMillis() + expirationTime));
        repository.save(passwordResetToken);
        sendEmail(email, token);
    }

    private void sendEmail(String to, String token) {
        String link = String.format(PASSWORD_RESET_MESSAGE_LINK_TEMPLATE, passwordResetUrl, token);
        String text = PASSWORD_RESET_MESSAGE_TEXT_TEMPLATE + link;
        mailSender.sendEmail(to, PASSWORD_RESET_MESSAGE_SUBJECT, text);
    }

    @Transactional
    public void resetPassword(PasswordResetTo passwordResetTo) {
        Assert.notNull(passwordResetTo, "passwordResetTo must not be null");
        PasswordResetToken passwordResetToken = checkToken(passwordResetTo.getToken());
        passwordResetToken.getUser().setPassword(PASSWORD_ENCODER.encode(passwordResetTo.getPassword()));
        repository.delete(passwordResetToken);
    }

    public PasswordResetToken checkToken(String token) {
        Assert.notNull(token, "token must not be null");
        PasswordResetToken passwordResetToken = repository.findByToken(token)
                .orElseThrow(() -> new NotFoundException("Not found password reset token=" + token));
        Date expiryDate = passwordResetToken.getExpiryDate();
        if (new Date().after(expiryDate)) {
            throw new PasswordResetException("Password reset token=" + passwordResetToken.getToken() + " expired");
        }
        return passwordResetToken;
    }

    public PasswordResetToken getByEmail(String email) {
        return repository.findByUserEmailIgnoreCase(email).orElseThrow(() ->
                new NotFoundException("Not found password reset token for email=" + email));
    }
}
