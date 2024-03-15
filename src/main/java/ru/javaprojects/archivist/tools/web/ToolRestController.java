package ru.javaprojects.archivist.tools.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import ru.javaprojects.archivist.documents.DocumentService;
import ru.javaprojects.archivist.tools.GroupAddSendingResult;
import ru.javaprojects.archivist.tools.GroupSendingTo;

@RestController
@Validated
@RequestMapping(value = ToolUIController.TOOLS_URL, produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
public class ToolRestController {
    private final DocumentService documentService;

    @InitBinder
    protected void initBinder(WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @PostMapping(value = "/group/sending/add", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public GroupAddSendingResult createGroupSending(@Valid GroupSendingTo groupSendingTo) {
        log.info("create {}", groupSendingTo);
        return documentService.createGroupSending(groupSendingTo);
    }
}
