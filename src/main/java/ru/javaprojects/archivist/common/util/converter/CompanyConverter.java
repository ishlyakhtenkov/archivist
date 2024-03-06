package ru.javaprojects.archivist.common.util.converter;

import lombok.AllArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.javaprojects.archivist.companies.CompanyRepository;
import ru.javaprojects.archivist.companies.model.Company;

@Component
@AllArgsConstructor
public class CompanyConverter implements Converter<String, Company> {
    private final CompanyRepository repository;

    @Override
    public Company convert(String id) {
        return id.isBlank() ? null : repository.getReferenceById(Long.parseLong(id));
    }
}
