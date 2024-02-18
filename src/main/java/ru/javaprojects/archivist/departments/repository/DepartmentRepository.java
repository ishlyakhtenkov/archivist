package ru.javaprojects.archivist.departments.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.archivist.common.NamedRepository;
import ru.javaprojects.archivist.departments.model.Department;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface DepartmentRepository extends NamedRepository<Department> {

    @EntityGraph(attributePaths = "employees")
    Optional<Department> findById(long id);

    List<Department> findAllByOrderByName();
}
