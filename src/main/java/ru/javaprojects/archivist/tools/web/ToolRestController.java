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
import ru.javaprojects.archivist.tools.GroupOperationResult;
import ru.javaprojects.archivist.tools.to.GroupAddSendingTo;
import ru.javaprojects.archivist.tools.to.GroupDeleteSendingTo;
import ru.javaprojects.archivist.tools.to.GroupSubscribeTo;
import ru.javaprojects.archivist.tools.to.GroupUnsubscribeTo;

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
    public GroupAddSendingResult createGroupSending(@Valid GroupAddSendingTo groupAddSendingTo) {
        log.info("create {}", groupAddSendingTo);
        return documentService.createGroupSending(groupAddSendingTo);
    }

    @PostMapping(value = "/group/sending/delete", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public GroupOperationResult deleteGroupSending(@Valid GroupDeleteSendingTo groupDeleteSendingTo) {
        log.info("group delete sending {}", groupDeleteSendingTo);
        return documentService.deleteGroupSending(groupDeleteSendingTo);
    }

    @PostMapping(value = "/group/unsubscribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public GroupOperationResult unsubscribeGroup(@Valid GroupUnsubscribeTo groupUnsubscribeTo) {
        log.info("group unsubscribe {}", groupUnsubscribeTo);
        return documentService.unsubscribeGroup(groupUnsubscribeTo);
    }

    @PostMapping(value = "/group/resubscribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public GroupOperationResult resubscribeGroup(@Valid GroupSubscribeTo groupSubscribeTo) {
        log.info("group resubscribe {}", groupSubscribeTo);
        return documentService.resubscribeGroup(groupSubscribeTo);
    }
}
