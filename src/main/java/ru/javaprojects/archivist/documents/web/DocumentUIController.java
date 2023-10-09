package ru.javaprojects.archivist.documents.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.javaprojects.archivist.documents.DocumentService;
import ru.javaprojects.archivist.documents.model.Document;

@Controller
@RequestMapping(DocumentUIController.DOCUMENTS_URL)
@AllArgsConstructor
@Slf4j
public class DocumentUIController {
    static final String DOCUMENTS_URL = "/documents";

    private final DocumentService service;

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
}
