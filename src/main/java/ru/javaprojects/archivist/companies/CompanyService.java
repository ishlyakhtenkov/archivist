package ru.javaprojects.archivist.companies;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
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

    public void delete(long id) {
        repository.deleteExisted(id);
    }
}
