package ru.javaprojects.archivist.documents.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.javaprojects.archivist.changenotices.model.Change;
import ru.javaprojects.archivist.common.util.FileUtil;
import ru.javaprojects.archivist.common.util.validation.NoHtml;
import ru.javaprojects.archivist.common.util.validation.ValidationUtil;
import ru.javaprojects.archivist.documents.DocumentService;
import ru.javaprojects.archivist.documents.model.Applicability;
import ru.javaprojects.archivist.documents.model.Content;
import ru.javaprojects.archivist.documents.model.Sending;
import ru.javaprojects.archivist.documents.model.Subscriber;
import ru.javaprojects.archivist.documents.to.ApplicabilityTo;
import ru.javaprojects.archivist.documents.to.ChangeTo;
import ru.javaprojects.archivist.documents.to.SendingTo;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

@RestController
@Validated
@RequestMapping(value = DocumentUIController.DOCUMENTS_URL, produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
public class DocumentRestController {
    private final DocumentService service;

    @Value("${content-path.documents}")
    private String contentPath;

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        log.info("delete document with id={}", id);
        service.delete(id);
    }

    @GetMapping("/{id}/applicabilities")
    public List<Applicability> getApplicabilities(@PathVariable long id) {
        log.info("get applicabilities for document with id={}", id);
        return service.getApplicabilities(id);
    }

    @DeleteMapping("/applicabilities/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteApplicability(@PathVariable long id) {
        log.info("delete applicability with id={}", id);
        service.deleteApplicability(id);
    }

    @PostMapping(value = "/applicabilities", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Applicability createApplicability(@Valid @RequestBody ApplicabilityTo applicabilityTo) {
        log.info("create {}", applicabilityTo);
        ValidationUtil.checkNew(applicabilityTo);
        return service.createApplicability(applicabilityTo);
    }

    @GetMapping("/{id}/content/latest")
    public Content getLatestContent(@PathVariable long id) {
        log.info("get latest content for document with id={}", id);
        return service.getLatestContent(id);
    }

    @GetMapping("/{id}/content/all")
    public List<Content> getAllContents(@PathVariable long id) {
        log.info("get all contents for document with id={}", id);
        return service.getAllContents(id);
    }

    @GetMapping(value = "/content/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> downloadContentFile(@RequestParam String fileLink) {
        log.info("download document file={}", fileLink);
        Resource resource = FileUtil.download(contentPath + fileLink);
        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=" +
                        URLEncoder.encode(Objects.requireNonNull(resource.getFilename()), StandardCharsets.UTF_8).replace("+", "%20"))
                .body(resource);
    }

    @DeleteMapping("/content/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteContent(@PathVariable long id) {
        log.info("delete content with id={}", id);
        service.deleteContent(id);
    }

    @PostMapping(value = "/content", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Content createContent(@RequestParam long id, @RequestParam @PositiveOrZero int changeNumber,
                                 @RequestPart @NotEmpty MultipartFile[] files) {
        log.info("create content with change number={} for document with id={}", changeNumber, id);
        return service.createContent(id, changeNumber, files);
    }

    @GetMapping("/{id}/subscribers")
    public List<Subscriber> getSubscribers(@PathVariable long id) {
        log.info("get subscribers for document with id={}", id);
        return service.getSubscribers(id);
    }

    @GetMapping("/{id}/sendings/by-company")
    public List<Sending> getSendings(@PathVariable long id, @RequestParam long companyId) {
        log.info("get sendings by company with id={} for document with id={}", companyId, id);
        return service.getSendings(id, companyId);
    }

    @PostMapping("/sendings")
    @ResponseStatus(HttpStatus.CREATED)
    public Sending createSending(@Valid @RequestBody SendingTo sendingTo) {
        log.info("create {}", sendingTo);
        ValidationUtil.checkNew(sendingTo);
        return service.createSending(sendingTo);
    }


    @PatchMapping("/subscribers/{id}/unsubscribe")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unsubscribe(@PathVariable long id, @RequestParam @NotBlank @NoHtml @Size(max = 256) String unsubscribeReason) {
        log.info("unsubscribe subscriber with id={}", id);
        service.unsubscribe(id, unsubscribeReason);
    }

    @PatchMapping("/subscribers/{id}/resubscribe")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resubscribe(@PathVariable long id) {
        log.info("resubscribe subscriber with id={}", id);
        service.resubscribe(id);
    }

    @DeleteMapping("/sendings/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSending(@PathVariable long id) {
        log.info("delete sending with id={}", id);
        service.deleteSending(id);
    }

    @GetMapping("/{id}/changes")
    public List<Change> getChanges(@PathVariable long id) {
        log.info("get changes for document with id={}", id);
        return service.getChanges(id);
    }

    @PostMapping(value = "/changes", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public Change createChange(@Valid @RequestBody ChangeTo changeTo) {
        log.info("create {}", changeTo);
        ValidationUtil.checkNew(changeTo);
        return service.createChange(changeTo);
    }

    @DeleteMapping("/changes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteChange(@PathVariable long id) {
        log.info("delete change with id={}", id);
        service.deleteChange(id);
    }
}
