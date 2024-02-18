package ru.javaprojects.archivist.common.web;

import lombok.AllArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import ru.javaprojects.archivist.departments.model.Department;
import ru.javaprojects.archivist.departments.repository.DepartmentRepository;

@Component
@AllArgsConstructor
public class DepartmentConverter implements Converter<String, Department> {
    private final DepartmentRepository repository;

    @Override
    public Department convert(String id) {
        return id.isBlank() ? null : repository.getReferenceById(Long.parseLong(id));
    }
}
