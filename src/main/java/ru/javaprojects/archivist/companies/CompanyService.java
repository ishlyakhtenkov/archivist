package ru.javaprojects.archivist.companies;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ru.javaprojects.archivist.common.error.exception.NotFoundException;
import ru.javaprojects.archivist.companies.model.Company;

@Service
@AllArgsConstructor
public class CompanyService {
    private final CompanyRepository repository;

    public Page<Company> getAll(Pageable pageable) {
        return repository.findAllByOrderByName(pageable);
    }

    public Page<Company> getAll(Pageable pageable, String keyword) {
        return repository.findAllByNameContainsIgnoreCaseOrderByName(pageable, keyword);
    }

    public Company get(long id) {
        return repository.getExisted(id);
    }

    public Company getByName(String name) {
        return repository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new NotFoundException("Not found company with name=" + name));
    }

    public void delete(long id) {
        repository.deleteExisted(id);
    }

    public void create(Company company) {
        Assert.notNull(company, "company must not be null");
        repository.save(company);
    }

    public void update(Company company) {
        Assert.notNull(company, "company must not be null");
        repository.getExisted(company.id());
        repository.save(company);
    }
}
