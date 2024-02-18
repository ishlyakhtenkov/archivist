package ru.javaprojects.archivist.departments;

import org.springframework.stereotype.Component;
import ru.javaprojects.archivist.common.util.validation.UniqueNameValidator;
import ru.javaprojects.archivist.departments.model.Department;
import ru.javaprojects.archivist.departments.repository.DepartmentRepository;

@Component
public class UniqueDepartmentNameValidator extends UniqueNameValidator<Department, DepartmentRepository> {
    public UniqueDepartmentNameValidator(DepartmentRepository repository) {
        super(repository);
    }
}
