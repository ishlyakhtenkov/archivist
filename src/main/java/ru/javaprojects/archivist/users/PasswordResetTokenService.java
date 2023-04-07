package ru.javaprojects.archivist.users;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PasswordResetTokenService {
    private final PasswordResetTokenRepository repository;
    private final UserService userService;

    @Value("${password-reset-token.expiration-time}")
    private long expirationTime;

    @Transactional
    public void sendPasswordResetEmail(String email) {
        Assert.notNull(email, "email must not be null");
        PasswordResetToken token = repository.findByUserEmailIgnoreCase(email)
                .orElse(new PasswordResetToken(userService.getByEmail(email)));
        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(new Date(System.currentTimeMillis() + expirationTime));
        repository.save(token);
        //TODO send email
    }



}
