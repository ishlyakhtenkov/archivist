package ru.javaprojects.archivist.departments.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.archivist.common.NamedRepository;
import ru.javaprojects.archivist.departments.model.Department;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface DepartmentRepository extends NamedRepository<Department> {

    @Query("SELECT d FROM Department d LEFT JOIN FETCH d.employees e WHERE d.id =:id ORDER BY e.lastName, e.firstName, e.middleName")
    Optional<Department> findByIdWithEmployees(long id);

    List<Department> findAllByOrderByName();
}
