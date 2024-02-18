package ru.javaprojects.archivist.departments.web;

import org.springframework.stereotype.Component;
import ru.javaprojects.archivist.common.util.validation.UniqueEmailValidator;
import ru.javaprojects.archivist.departments.model.Employee;
import ru.javaprojects.archivist.departments.repository.EmployeeRepository;

@Component
public class UniqueEmployeeEmailValidator extends UniqueEmailValidator<Employee, EmployeeRepository> {
    public UniqueEmployeeEmailValidator(EmployeeRepository repository) {
        super(repository);
    }
}
