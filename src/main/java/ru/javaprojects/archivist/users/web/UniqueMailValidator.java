package ru.javaprojects.archivist.users.web;

import lombok.AllArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import ru.javaprojects.archivist.users.HasIdAndEmail;
import ru.javaprojects.archivist.users.UserRepository;

import static ru.javaprojects.archivist.common.error.Constants.DUPLICATE_EMAIL_MESSAGE;
import static ru.javaprojects.archivist.common.error.Constants.DUPLICATE_ERROR_CODE;

@Component
@AllArgsConstructor
public class UniqueMailValidator implements org.springframework.validation.Validator {
    private final UserRepository repository;

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return HasIdAndEmail.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        HasIdAndEmail user = ((HasIdAndEmail) target);
        if (StringUtils.hasText(user.getEmail())) {
            repository.findByEmailIgnoreCase(user.getEmail())
                    .ifPresent(dbUser -> {
                        if (user.isNew() || !user.getId().equals(dbUser.getId())) {
                            errors.rejectValue("email", DUPLICATE_ERROR_CODE, DUPLICATE_EMAIL_MESSAGE);
                        }
                    });
        }
    }
}
