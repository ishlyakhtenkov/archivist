package ru.javaprojects.archivist.departments.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.javaprojects.archivist.departments.model.Department;
import ru.javaprojects.archivist.departments.service.DepartmentService;

import java.util.List;

@RestController
@RequestMapping(value = DepartmentUIController.DEPARTMENTS_URL, produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
public class DepartmentRestController {
    private final DepartmentService service;

    @GetMapping("/list")
    public List<Department> getAll() {
        log.info("get departments list");
        return service.getAll();
    }
}
