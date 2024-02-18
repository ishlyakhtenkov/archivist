package ru.javaprojects.archivist.common.util.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import ru.javaprojects.archivist.common.EmailedRepository;
import ru.javaprojects.archivist.common.HasIdAndEmail;

import java.util.Objects;

import static ru.javaprojects.archivist.common.error.Constants.DUPLICATE_ERROR_CODE;

@RequiredArgsConstructor
public abstract class UniqueEmailValidator<E extends HasIdAndEmail, R extends EmailedRepository<E>> implements org.springframework.validation.Validator {
    protected final R repository;

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return HasIdAndEmail.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        HasIdAndEmail emailed = ((HasIdAndEmail) target);
        if (StringUtils.hasText(emailed.getEmail())) {
            repository.findByEmailIgnoreCase(emailed.getEmail())
                    .ifPresent(dbEmailed -> {
                        if (emailed.isNew() || !Objects.equals(emailed.getId(), dbEmailed.getId())) {
                            errors.rejectValue("email", DUPLICATE_ERROR_CODE, emailed.getClass().getSimpleName() + " with this email already exists");
                        }
                    });
        }
    }
}
