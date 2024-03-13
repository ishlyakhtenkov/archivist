package ru.javaprojects.archivist.companies.web;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
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
import ru.javaprojects.archivist.companies.model.Company;

@Controller
@RequestMapping(CompanyUIController.COMPANIES_URL)
@AllArgsConstructor
@Slf4j
public class CompanyUIController {
    static final String COMPANIES_URL = "/companies";

    private final CompanyService service;
    private final UniqueCompanyNameValidator nameValidator;

    @InitBinder("company")
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(nameValidator);
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping
    public String getAll(@RequestParam(value = "keyword", required = false) String keyword,
                         @PageableDefault Pageable pageable, Model model, RedirectAttributes redirectAttributes) {
        Page<Company> companies;
        if (keyword != null) {
            if (keyword.isBlank()) {
                return "redirect:/companies";
            }
            log.info("get companies (pageNumber={}, pageSize={}, keyword={})", pageable.getPageNumber(),
                    pageable.getPageSize(), keyword);
            companies = service.getAll(pageable, keyword.trim());
        } else  {
            log.info("get companies (pageNumber={}, pageSize={})", pageable.getPageNumber(), pageable.getPageSize());
            companies = service.getAll(pageable);
        }
        if (companies.getContent().isEmpty() && companies.getTotalElements() != 0) {
            if (keyword != null) {
                redirectAttributes.addAttribute("keyword", keyword);
            }
            return "redirect:/companies";
        }
        model.addAttribute("companies", companies);
        return "companies/companies";
    }

    @GetMapping("/{id}")
    public String get(@PathVariable long id, Model model) {
        log.info("get company with id={}", id);
        model.addAttribute("company", service.getWithContactPersons(id));
        return "companies/company";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        log.info("show company add form");
        model.addAttribute("company", new Company());
        return "companies/company-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable long id, Model model) {
        log.info("show edit form for company with id={}", id);
        model.addAttribute("company", service.getWithContactPersons(id));
        return "companies/company-form";
    }

    @PostMapping
    public String createOrUpdate(@Valid Company company, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "companies/company-form";
        }
        boolean isNew = company.isNew();
        log.info((isNew ? "create" : "update") + " {}", company);
        if (isNew) {
            service.create(company);
        } else {
            service.update(company);
        }
        redirectAttributes.addFlashAttribute("action", "Company " + company.getName() +
                (isNew ? " was created" : " was updated"));
        return "redirect:/companies/" + company.getId();
    }
}
