package ru.javaprojects.archivist.departments.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.javaprojects.archivist.common.error.NotFoundException;
import ru.javaprojects.archivist.departments.model.Department;
import ru.javaprojects.archivist.departments.model.Employee;
import ru.javaprojects.archivist.departments.repository.DepartmentRepository;
import ru.javaprojects.archivist.departments.to.DepartmentCreateTo;
import ru.javaprojects.archivist.departments.to.DepartmentUpdateTo;

import java.util.List;

import static ru.javaprojects.archivist.departments.DepartmentUtil.*;

@Service
@AllArgsConstructor
public class DepartmentService {
    private final DepartmentRepository repository;
    private final EmployeeService employeeService;

    public List<Department> getAll() {
        return repository.findAllByOrderByName();
    }

    public List<Department> getAllWithBoss() {
        return repository.findAllWithBossByOrderByName();
    }

    public Department getWithEmployees(long id) {
        return repository.findByIdWithEmployees(id).orElseThrow(() -> new NotFoundException("Entity with id=" + id + " not found"));
    }

    public Department get(long id) {
        return repository.getExisted(id);
    }

    public Department getWithBoss(long id) {
        return repository.findWithBossById(id)
                .orElseThrow(() -> new NotFoundException("Entity with id=" + id + " not found"));
    }

    public Department getByName(String name) {
        return repository.findWithBossByNameIgnoreCase(name)
                .orElseThrow(() -> new NotFoundException("Not found department with name=" + name));
    }

    @Transactional
    public Department create(DepartmentCreateTo departmentCreateTo) {
        Assert.notNull(departmentCreateTo, "departmentCreateTo must not be null");
        Department department = repository.save(createNewDepartmentFromTo(departmentCreateTo));
        Employee boss = employeeService.create(createNewEmployeeFromTo(departmentCreateTo, department));
        department.setBoss(boss);
        return department;
    }

    @Transactional
    public void update(DepartmentUpdateTo departmentUpdateTo) {
        Assert.notNull(departmentUpdateTo, "departmentUpdateTo must not be null");
        Department department = get(departmentUpdateTo.getId());
        updateFromTo(department, departmentUpdateTo);
    }

    public void delete(long id) {
        repository.deleteExisted(id);
    }
}
