package ru.javaprojects.archivist.companies;

import lombok.AllArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import ru.javaprojects.archivist.companies.model.Company;

import java.util.Objects;

@Component
@AllArgsConstructor
public class UniqueNameValidator implements org.springframework.validation.Validator {
    public static final String DUPLICATE_NAME_ERROR_CODE = "Duplicate";
    public static final String DUPLICATE_NAME_MESSAGE = "Company with this name already exists";

    private final CompanyRepository repository;

    @Override
    public boolean supports(@NonNull Class<?> clazz) {
        return Company.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(@NonNull Object target, @NonNull Errors errors) {
        Company company = ((Company) target);
        if (StringUtils.hasText(company.getName())) {
            repository.findByNameIgnoreCase(company.getName())
                    .ifPresent(dbCompany -> {
                        if (company.isNew() || !Objects.equals(company.getId(), dbCompany.getId())) {
                            errors.rejectValue("name", DUPLICATE_NAME_ERROR_CODE, DUPLICATE_NAME_MESSAGE);
                        }
                    });
        }
    }
}
