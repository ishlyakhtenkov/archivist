package ru.javaprojects.archivist.common.util.validation;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import ru.javaprojects.archivist.common.NamedRepository;
import ru.javaprojects.archivist.common.model.NamedEntity;

import java.util.Objects;

import static ru.javaprojects.archivist.common.error.Constants.DUPLICATE_ERROR_CODE;

@RequiredArgsConstructor
public abstract class UniqueNameValidator<E extends NamedEntity, R extends NamedRepository<E>> implements org.springframework.validation.Validator {
    protected final R repository;

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return NamedEntity.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        NamedEntity named = ((NamedEntity) target);
        if (StringUtils.hasText(named.getName())) {
            repository.findByNameIgnoreCase(named.getName())
                    .ifPresent(dbNamed -> {
                        if (named.isNew() || !Objects.equals(named.getId(), dbNamed.getId())) {
                            errors.rejectValue("name", DUPLICATE_ERROR_CODE, named.getClass().getSimpleName() + " with this name already exists");
                        }
                    });
        }
    }
}
