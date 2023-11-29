package ru.javaprojects.archivist.changenotices.web;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.javaprojects.archivist.changenotices.ChangeNoticeService;
import ru.javaprojects.archivist.changenotices.ChangeNoticeUtil;
import ru.javaprojects.archivist.changenotices.model.ChangeNotice;
import ru.javaprojects.archivist.changenotices.model.ChangeReasonCode;
import ru.javaprojects.archivist.changenotices.to.ChangeNoticeTo;
import ru.javaprojects.archivist.common.util.FileUtil;
import ru.javaprojects.archivist.departments.DepartmentService;

@Controller
@RequestMapping(ChangeNoticeUIController.CHANGE_NOTICES_URL)
@RequiredArgsConstructor
@Slf4j
public class ChangeNoticeUIController {
    static final String CHANGE_NOTICES_URL = "/change-notices";

    private final ChangeNoticeService service;
    private final DepartmentService departmentService;
    private final UniqueChangeNoticeNameValidator nameValidator;
    private final ChangeNoticeUtil changeNoticeUtil;

    @Value("${content-path.change-notices}")
    private String contentPath;


    @InitBinder("changeNoticeTo")
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(nameValidator);
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping
    public String getAll(@RequestParam(value = "keyword", required = false) String keyword,
                         @PageableDefault Pageable pageable, Model model, RedirectAttributes redirectAttributes) {
        Page<ChangeNotice> changeNotices;
        if (keyword != null) {
            if (keyword.isBlank()) {
                return "redirect:/change-notices";
            }
            log.info("getAll(pageNumber={}, pageSize={}, keyword={})", pageable.getPageNumber(), pageable.getPageSize(), keyword);
            changeNotices = service.getAll(pageable, keyword.trim());
        } else  {
            log.info("getAll(pageNumber={}, pageSize={})", pageable.getPageNumber(), pageable.getPageSize());
            changeNotices = service.getAll(pageable);
        }
        if (changeNotices.getContent().isEmpty() && changeNotices.getTotalElements() != 0) {
            if (keyword != null) {
                redirectAttributes.addAttribute("keyword", keyword);
            }
            return "redirect:/change-notices";
        }
        model.addAttribute("changeNotices", changeNotices);
        return "change-notices/change-notices";
    }

    @GetMapping("/{id}")
    public String get(@PathVariable long id, Model model) {
        log.info("get {}", id);
        model.addAttribute("changeNotice", service.getWithChanges(id));
        return "change-notices/change-notice";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        log.info("show change notice add form");
        model.addAttribute("changeNoticeTo", new ChangeNoticeTo());
        addDataForChangeNoticeCardToModel(model);
        return "change-notices/change-notice-form";
    }

    @PostMapping
    public String createOrUpdate(@Valid ChangeNoticeTo changeNoticeTo, BindingResult result, Model model,
                                 RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            addDataForChangeNoticeCardToModel(model);
            if (!changeNoticeTo.isNew() && changeNoticeTo.getFile() == null) {
                model.addAttribute("file", service.get(changeNoticeTo.getId()).getFile());
            }
            return "change-notices/change-notice-form";
        }
        boolean isNew = changeNoticeTo.isNew();
        log.info((isNew ? "create" : "update") + " {}", changeNoticeTo);
        ChangeNotice changeNotice;
        if (isNew) {
            changeNotice = service.create(changeNoticeTo);
        } else {
            changeNotice = service.update(changeNoticeTo);
        }
        redirectAttributes.addFlashAttribute("action", "Change notice " + changeNotice.getName() +
                (isNew ? " was created" : " was updated"));
        return "redirect:/change-notices/" + changeNotice.getId();
    }

    private void addDataForChangeNoticeCardToModel(Model model) {
        model.addAttribute("changeReasonCodes", ChangeReasonCode.values());
        model.addAttribute("developers", departmentService.getAll());
    }

    @GetMapping(value = "/download", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> downloadFile(@RequestParam String fileLink) {
        log.info("download file {}", fileLink);
        Resource resource = FileUtil.download(contentPath + fileLink);
        return ResponseEntity.ok()
                .header("Content-Disposition", "inline; filename=" + resource.getFilename())
                .body(resource);
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable long id, Model model) {
        log.info("show change notice={} edit form", id);
        ChangeNotice changeNotice = service.getWithChanges(id);
        model.addAttribute("changeNoticeTo", changeNoticeUtil.asTo(changeNotice));
        model.addAttribute("file", changeNotice.getFile());
        addDataForChangeNoticeCardToModel(model);
        return "change-notices/change-notice-form";
    }
}
