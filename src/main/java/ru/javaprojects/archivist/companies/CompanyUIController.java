package ru.javaprojects.archivist.companies;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.javaprojects.archivist.companies.model.Company;

@Controller
@RequestMapping(CompanyUIController.COMPANIES_URL)
@AllArgsConstructor
@Slf4j
public class CompanyUIController {
    static final String COMPANIES_URL = "/companies";

    private final CompanyService service;

    @GetMapping
    public String getAll(@RequestParam(value = "keyword", required = false) String keyword,
                         @PageableDefault Pageable pageable, Model model) {
        Page<Company> companies;
        if (keyword != null) {
            if (keyword.isBlank()) {
                return "redirect:/companies";
            }
            log.info("getAll(pageNumber={}, pageSize={}, keyword={})", pageable.getPageNumber(), pageable.getPageSize(), keyword);
            companies = service.getAll(pageable, keyword.trim());
        } else  {
            log.info("getAll(pageNumber={}, pageSize={})", pageable.getPageNumber(), pageable.getPageSize());
            companies = service.getAll(pageable);
        }
        if (companies.getContent().isEmpty() && companies.getTotalElements() != 0) {
            return "redirect:/companies";
        }
        model.addAttribute("companies", companies);
        return "companies/companies";
    }
}
