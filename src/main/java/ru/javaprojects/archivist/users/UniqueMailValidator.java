package ru.javaprojects.archivist.users;

import lombok.AllArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;

@Component
@AllArgsConstructor
public class UniqueMailValidator implements org.springframework.validation.Validator {
    public static final String DUPLICATE_EMAIL_ERROR_CODE = "Duplicate";
    public static final String DUPLICATE_EMAIL_MESSAGE = "User with this email already exists";

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
                            errors.rejectValue("email", DUPLICATE_EMAIL_ERROR_CODE, DUPLICATE_EMAIL_MESSAGE);
                        }
                    });
        }
    }
}
