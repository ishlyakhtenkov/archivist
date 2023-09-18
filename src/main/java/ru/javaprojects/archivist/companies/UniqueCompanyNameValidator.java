package ru.javaprojects.archivist.companies;

import org.springframework.stereotype.Component;
import ru.javaprojects.archivist.common.util.validation.UniqueNameValidator;
import ru.javaprojects.archivist.companies.model.Company;

@Component
public class UniqueCompanyNameValidator extends UniqueNameValidator<Company, CompanyRepository> {
    public UniqueCompanyNameValidator(CompanyRepository repository) {
        super(repository);
    }
}
