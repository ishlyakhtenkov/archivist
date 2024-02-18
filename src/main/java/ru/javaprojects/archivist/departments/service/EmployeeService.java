package ru.javaprojects.archivist.departments.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.javaprojects.archivist.common.error.NotFoundException;
import ru.javaprojects.archivist.departments.model.Employee;
import ru.javaprojects.archivist.departments.repository.EmployeeRepository;

@Service
@AllArgsConstructor
public class EmployeeService {
    private final EmployeeRepository repository;

    public Employee get(long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Entity with id=" + id + " not found"));
    }

    public void create(Employee employee) {
        Assert.notNull(employee, "employee must not be null");
        repository.save(employee);
    }

    @Transactional // just to make one select by id instead of two by Hibernate
    public void update(Employee employee) {
        Assert.notNull(employee, "employee must not be null");
        repository.getExisted(employee.id());
        repository.save(employee);
    }

    public void delete(long id) {
        repository.deleteExisted(id);
    }

    @Transactional
    public void fire(long id, boolean fired) {
        Employee employee = repository.getExisted(id);
        employee.setFired(fired);
    }
}
