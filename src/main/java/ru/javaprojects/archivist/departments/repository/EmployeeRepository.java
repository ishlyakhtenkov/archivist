package ru.javaprojects.archivist.departments.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.archivist.common.EmailedRepository;
import ru.javaprojects.archivist.departments.model.Employee;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface EmployeeRepository extends EmailedRepository<Employee> {

    @EntityGraph(attributePaths = "department")
    Optional<Employee> findById(long id);

    Optional<Employee> findByEmailIgnoreCase(String email);

    List<Employee> findAllByDepartment_IdOrderByLastNameAscFirstNameAscMiddleName(long departmentId);
}
