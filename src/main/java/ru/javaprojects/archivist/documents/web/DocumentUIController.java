package ru.javaprojects.archivist.documents.web;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.javaprojects.archivist.companies.CompanyService;
import ru.javaprojects.archivist.departments.Department;
import ru.javaprojects.archivist.departments.DepartmentService;
import ru.javaprojects.archivist.documents.DocumentService;
import ru.javaprojects.archivist.documents.model.Document;
import ru.javaprojects.archivist.documents.model.Letter;
import ru.javaprojects.archivist.documents.model.Status;
import ru.javaprojects.archivist.documents.model.Type;

import static ru.javaprojects.archivist.common.util.validation.ValidationUtil.checkNew;

@Controller
@RequestMapping(DocumentUIController.DOCUMENTS_URL)
@AllArgsConstructor
@Slf4j
public class DocumentUIController {
    static final String DOCUMENTS_URL = "/documents";

    private final DocumentService service;
    private final CompanyService companyService;
    private final DepartmentService departmentService;
    private final UniqueDocumentDecimalNumberValidator decimalNumberValidator;
    private final UniqueDocumentInventoryNumberValidator inventoryNumberValidator;

    @InitBinder("document")
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(decimalNumberValidator, inventoryNumberValidator);
    }

    @GetMapping
    public String getAll(@RequestParam(value = "keyword", required = false) String keyword,
                         @PageableDefault Pageable pageable, Model model, RedirectAttributes redirectAttributes) {
        Page<Document> documents;
        if (keyword != null) {
            if (keyword.isBlank()) {
                return "redirect:/documents";
            }
            log.info("getAll(pageNumber={}, pageSize={}, keyword={})", pageable.getPageNumber(), pageable.getPageSize(), keyword);
            documents = service.getAll(pageable, keyword.trim());
        } else  {
            log.info("getAll(pageNumber={}, pageSize={})", pageable.getPageNumber(), pageable.getPageSize());
            documents = service.getAll(pageable);
        }
        if (documents.getContent().isEmpty() && documents.getTotalElements() != 0) {
            if (keyword != null) {
                redirectAttributes.addAttribute("keyword", keyword);
            }
            return "redirect:/documents";
        }
        model.addAttribute("documents", documents);
        return "documents/documents";
    }

    @GetMapping("/{id}")
    public String documentDetails(@PathVariable long id, Model model) {
        log.info("show document details {}", id);
        model.addAttribute("document", service.get(id));
        return "documents/document-details";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        log.info("show document add form");
        model.addAttribute("document", new Document());
        addDataForDocumentCardToModel(model);
        return "documents/document-form";
    }

    @PostMapping
    public String createOrUpdate(@Valid Document document, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            addDataForDocumentCardToModel(model);
            return "documents/document-form";
        }
        boolean isNew = document.isNew();
        log.info((isNew ? "create" : "update") + " {}", document);
        service.createOrUpdate(document);
        redirectAttributes.addFlashAttribute("action", "Document " + document.getDecimalNumber() +
                (isNew ? " was created" : " was updated"));
        return "redirect:/documents/" + document.getId();
    }

    private void addDataForDocumentCardToModel(Model model) {
        model.addAttribute("statuses", Status.values());
        model.addAttribute("letters", Letter.values());
        model.addAttribute("types", Type.values());
        model.addAttribute("companies", companyService.getAll());
        model.addAttribute("developers", departmentService.getAll());
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable long id, Model model) {
        log.info("show document={} edit form", id);
        model.addAttribute("document", service.get(id));
        addDataForDocumentCardToModel(model);
        return "documents/document-form";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable long id, RedirectAttributes redirectAttributes) {
        log.info("delete document={}", id);
        Document document = service.get(id);
        service.delete(id);
        redirectAttributes.addFlashAttribute("action", "Document " + document.getDecimalNumber() + " was deleted");
        return "redirect:/documents";
    }
}