package ru.javaprojects.archivist.companies.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.javaprojects.archivist.companies.CompanyService;
import ru.javaprojects.archivist.companies.model.Company;

import java.util.List;

@RestController
@RequestMapping(value = CompanyUIController.COMPANIES_URL, produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
public class CompanyRestController {
    private final CompanyService service;

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        log.info("delete company with id={}", id);
        service.delete(id);
    }

    @GetMapping("/list")
    public List<Company> getAll() {
        log.info("get companies list");
        return service.getAll();
    }
}
