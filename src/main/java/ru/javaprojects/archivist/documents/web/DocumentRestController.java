package ru.javaprojects.archivist.documents.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.javaprojects.archivist.common.util.FileUtil;
import ru.javaprojects.archivist.documents.DocumentService;
import ru.javaprojects.archivist.documents.model.Applicability;
import ru.javaprojects.archivist.documents.model.Content;
import ru.javaprojects.archivist.documents.to.ApplicabilityTo;

import java.util.List;

@RestController
@RequestMapping(value = DocumentUIController.DOCUMENTS_URL, produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
public class DocumentRestController {
    private final DocumentService service;

    @Value("${content-path.documents}")
    private String contentPath;

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

    @GetMapping("/{id}/content/latest")
    public Content getLatestContent(@PathVariable long id) {
        log.info("get latest content for document {}", id);
        return service.getLatestContent(id);
    }

    @GetMapping("/{id}/content/all")
    public List<Content> getAllContents(@PathVariable long id) {
        log.info("get all contents for document {}", id);
        return service.getAllContents(id);
    }

    @GetMapping(value = "/content/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> downloadContentFile(@RequestParam String fileLink) {
        log.debug("download file {}", fileLink);
        Resource resource = FileUtil.download(contentPath + fileLink);
        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=" + resource.getFilename())
                .body(resource);
    }

    @DeleteMapping("/content/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteContent(@PathVariable long id) {
        log.info("delete content {}", id);
        service.deleteContent(id);
    }
}
