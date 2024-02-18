package ru.javaprojects.archivist.departments.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.javaprojects.archivist.departments.service.EmployeeService;

@RestController
@RequestMapping(value = EmployeeUIController.EMPLOYEES_URL, produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
public class EmployeeRestController {
    private final EmployeeService service;

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        log.info("delete {}", id);
        service.delete(id);
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enable(@PathVariable long id, @RequestParam boolean fired) {
        log.info(fired ? "fire {}" : "unfire {}", id);
        service.fire(id, fired);
    }
}
