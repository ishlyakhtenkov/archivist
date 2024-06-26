package ru.javaprojects.archivist.albums.web;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.javaprojects.archivist.albums.AlbumService;
import ru.javaprojects.archivist.albums.model.Album;
import ru.javaprojects.archivist.albums.model.Issuance;
import ru.javaprojects.archivist.albums.to.IssuanceTo;
import ru.javaprojects.archivist.common.error.IllegalRequestDataException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(value = AlbumUIController.ALBUMS_URL, produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
public class AlbumRestController {
    private final AlbumService service;

    @GetMapping("/by-document")
    public List<Album> getAlbums(@RequestParam long documentId) {
        log.info("get albums by document with id={}", documentId);
        return service.getAlbums(documentId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        log.info("delete album with id={}", id);
        service.delete(id);
    }

    @GetMapping("/{id}/issuances")
    public List<Issuance> getIssuances(@PathVariable long id) {
        log.info("get issuances for album with id={}", id);
        return service.getIssuances(id);
    }

    @DeleteMapping("/issuances/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteIssuance(@PathVariable long id) {
        log.info("delete issuance with id={}", id);
        service.deleteIssuance(id);
    }

    @PatchMapping("/{id}/return")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void returnAlbum(@PathVariable long id, @RequestParam LocalDate returned) {
        log.info("return album with id={}, returned date={}", id, returned);
        service.returnAlbum(id, returned);
    }

    @PostMapping("/{id}/issue")
    @ResponseStatus(HttpStatus.CREATED)
    public Issuance issueAlbum(@PathVariable long id, @Valid @RequestBody IssuanceTo issuanceTo) {
        log.info("issue album ({})", issuanceTo);
        if (id != issuanceTo.getAlbumId()) {
            throw new IllegalRequestDataException("IssuanceTo must has albumId=" + id);
        }
        return service.issueAlbum(issuanceTo);
    }
}
