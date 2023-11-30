package ru.javaprojects.archivist.changenotices.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.javaprojects.archivist.changenotices.ChangeNoticeService;
import ru.javaprojects.archivist.changenotices.model.Change;

import java.util.List;

@RestController
@RequestMapping(value = ChangeNoticeUIController.CHANGE_NOTICES_URL, produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
public class ChangeNoticeRestController {
    private final ChangeNoticeService service;

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        log.info("delete {}", id);
        service.delete(id);
    }

    @GetMapping("/changes/by-change-notice")
    public List<Change> getChangesByName(@RequestParam String changeNoticeName) {
        log.info("get changes for change notice {}", changeNoticeName);
        return service.getChanges(changeNoticeName);
    }
}
