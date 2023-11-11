package ru.javaprojects.archivist.companies;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ru.javaprojects.archivist.common.error.NotFoundException;
import ru.javaprojects.archivist.companies.model.Company;

import java.util.List;

@Service
@AllArgsConstructor
public class CompanyService {
    private final CompanyRepository repository;

    public List<Company> getAll() {
        return repository.findAll(Sort.by("name"));
    }

    public Page<Company> getAll(Pageable pageable) {
        return repository.findAllByOrderByName(pageable);
    }

    public Page<Company> getAll(Pageable pageable, String keyword) {
        return repository.findAllByNameContainsIgnoreCaseOrderByName(pageable, keyword);
    }

    public Company get(long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Entity with id=" + id + " not found"));
    }

    public Company getByName(String name) {
        return repository.findWihContactPersonsByNameIgnoreCase(name)
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
