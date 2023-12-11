package ru.javaprojects.archivist.users.password_reset;

import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.archivist.common.BaseRepository;

import java.util.Optional;

@Transactional(readOnly = true)
public interface PasswordResetTokenRepository extends BaseRepository<PasswordResetToken> {

    Optional<PasswordResetToken> findByUserEmailIgnoreCase(String email);

    Optional<PasswordResetToken> findByToken(String token);
}
