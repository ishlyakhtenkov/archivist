package ru.javaprojects.archivist.departments;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.javaprojects.archivist.departments.model.Department;
import ru.javaprojects.archivist.departments.service.DepartmentService;

@Controller
@RequestMapping(DepartmentUIController.DEPARTMENTS_URL)
@AllArgsConstructor
@Slf4j
public class DepartmentUIController {
    public static final String DEPARTMENTS_URL = "/departments";

    private final DepartmentService service;
    private final UniqueDepartmentNameValidator nameValidator;

    @InitBinder("department")
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(nameValidator);
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping
    public String getAll(Model model) {
        log.info("getAll");
        model.addAttribute("departments", service.getAll());
        return "departments/departments";
    }

    @GetMapping("/{id}")
    public String get(@PathVariable long id, Model model) {
        log.info("get {}", id);
        model.addAttribute("department", service.getWithEmployees(id));
        return "departments/department";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        log.info("show department add form");
        model.addAttribute("department", new Department());
        return "departments/department-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable long id, Model model) {
        log.info("show department={} edit form", id);
        model.addAttribute("department", service.get(id));
        return "departments/department-form";
    }

    @PostMapping
    public String createOrUpdate(@Valid Department department, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "departments/department-form";
        }
        boolean isNew = department.isNew();
        log.info((isNew ? "create" : "update") + " {}", department);
        if (isNew) {
            service.create(department);
        } else {
            service.update(department);
        }
        redirectAttributes.addFlashAttribute("action", "Department " + department.getName() +
                (isNew ? " was created" : " was updated"));
        return "redirect:/departments";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable long id, RedirectAttributes redirectAttributes) {
        log.info("delete department={}", id);
        Department department = service.get(id);
        service.delete(id);
        redirectAttributes.addFlashAttribute("action", "Department " + department.getName() + " was deleted");
        return "redirect:/departments";
    }
}
