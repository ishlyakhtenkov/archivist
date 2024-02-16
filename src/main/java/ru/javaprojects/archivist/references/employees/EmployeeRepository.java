package ru.javaprojects.archivist.references.employees;

import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.archivist.common.BaseRepository;

@Transactional(readOnly = true)
public interface EmployeeRepository extends BaseRepository<Employee> {

}
