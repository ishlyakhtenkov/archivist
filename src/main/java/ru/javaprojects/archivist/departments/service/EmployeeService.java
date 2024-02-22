package ru.javaprojects.archivist.departments.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.javaprojects.archivist.common.error.IllegalRequestDataException;
import ru.javaprojects.archivist.common.error.NotFoundException;
import ru.javaprojects.archivist.departments.model.Employee;
import ru.javaprojects.archivist.departments.repository.EmployeeRepository;

import java.util.List;
import java.util.Objects;

@Service
@AllArgsConstructor
public class EmployeeService {
    private final EmployeeRepository repository;

    public Employee get(long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Entity with id=" + id + " not found"));
    }

    public Employee getByEmail(String email) {
        return repository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new NotFoundException("Not found employee with email=" + email));
    }

    public List<Employee> getAllByDepartment(long departmentId) {
        return repository.findAllByDepartment_IdOrderByLastNameAscFirstNameAscMiddleName(departmentId);
    }

    public Employee create(Employee employee) {
        Assert.notNull(employee, "employee must not be null");
        return repository.save(employee);
    }

    @Transactional // just to make one select by id instead of two by Hibernate
    public void update(Employee employee) {
        Assert.notNull(employee, "employee must not be null");
        repository.getExisted(employee.id());
        repository.save(employee);
    }

    public void delete(long id) {
        Employee employee = get(id);
        if (Objects.equals(employee.getId(), employee.getDepartment().getBoss().getId())) {
            throw new IllegalRequestDataException("Cannot delete boss of the department");
        }
        repository.delete(id);
    }

    @Transactional
    public void fire(long id, boolean fired) {
        Employee employee = repository.getExisted(id);
        employee.setFired(fired);
    }
}
