package ru.javaprojects.archivist.documents.web;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.javaprojects.archivist.documents.DocumentService;
import ru.javaprojects.archivist.documents.model.Applicability;
import ru.javaprojects.archivist.documents.to.ApplicabilityTo;

import java.util.List;

@RestController
@RequestMapping(value = DocumentUIController.DOCUMENTS_URL, produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
public class DocumentRestController {
    private final DocumentService service;

    @GetMapping("/{id}/applicabilities")
    public List<Applicability> getApplicabilities(@PathVariable long id) {
        log.info("get applicabilities for document {}", id);
        return service.getApplicabilities(id);
    }

    @DeleteMapping("/applicabilities/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteApplicability(@PathVariable long id) {
        log.info("delete applicability {}", id);
        service.deleteApplicability(id);
    }

    @PostMapping(value = "/applicabilities", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Applicability createApplicability(@Valid @RequestBody ApplicabilityTo applicabilityTo) {
        log.info("create {}", applicabilityTo);
        return service.createApplicability(applicabilityTo);
    }
}
