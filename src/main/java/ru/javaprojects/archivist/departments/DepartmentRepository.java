package ru.javaprojects.archivist.departments;

import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.archivist.common.NamedRepository;
import ru.javaprojects.archivist.departments.model.Department;

import java.util.List;

@Transactional(readOnly = true)
public interface DepartmentRepository extends NamedRepository<Department> {
    List<Department> findAllByOrderByName();
}
