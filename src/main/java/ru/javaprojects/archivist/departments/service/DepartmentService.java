package ru.javaprojects.archivist.departments.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.javaprojects.archivist.common.error.NotFoundException;
import ru.javaprojects.archivist.departments.model.Department;
import ru.javaprojects.archivist.departments.repository.DepartmentRepository;

import java.util.List;

@Service
@AllArgsConstructor
public class DepartmentService {
    private final DepartmentRepository repository;

    public List<Department> getAll() {
        return repository.findAllByOrderByName();
    }

    public Department getWithEmployees(long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Entity with id=" + id + " not found"));
    }

    public Department get(long id) {
        return repository.getExisted(id);
    }

    public Department getByName(String name) {
        return repository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new NotFoundException("Not found department with name=" + name));
    }

    public void create(Department department) {
        Assert.notNull(department, "department must not be null");
        repository.save(department);
    }

    @Transactional // just to make one select by id instead of two by Hibernate
    public void update(Department department) {
        Assert.notNull(department, "department must not be null");
        get(department.id());
        repository.save(department);
    }

    public void delete(long id) {
        repository.deleteExisted(id);
    }
}
