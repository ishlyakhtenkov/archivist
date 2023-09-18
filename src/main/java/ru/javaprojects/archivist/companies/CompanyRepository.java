package ru.javaprojects.archivist.companies;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.archivist.common.NamedRepository;
import ru.javaprojects.archivist.companies.model.Company;

import java.util.Optional;

@Transactional(readOnly = true)
public interface CompanyRepository extends NamedRepository<Company> {

    Page<Company> findAllByOrderByName(Pageable pageable);

    Page<Company> findAllByNameContainsIgnoreCaseOrderByName(Pageable pageable, String keyword);

    @EntityGraph(attributePaths = "contactPersons")
    Optional<Company> findById(long id);
}
