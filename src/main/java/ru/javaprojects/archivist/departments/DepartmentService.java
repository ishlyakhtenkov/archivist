package ru.javaprojects.archivist.departments;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import ru.javaprojects.archivist.common.error.NotFoundException;

import java.util.List;

@Service
@AllArgsConstructor
public class DepartmentService {
    private final DepartmentRepository repository;

    public List<Department> getAll() {
        return repository.findAllByOrderByName();
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

    public void update(Department department) {
        Assert.notNull(department, "department must not be null");
        repository.getExisted(department.id());
        repository.save(department);
    }

    public void delete(long id) {
        repository.deleteExisted(id);
    }
}
