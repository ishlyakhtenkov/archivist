package ru.javaprojects.archivist.departments;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.javaprojects.archivist.departments.model.Department;

import static ru.javaprojects.archivist.common.util.validation.ValidationUtil.checkNew;

@Controller
@RequestMapping(DepartmentUIController.DEPARTMENTS_URL)
@AllArgsConstructor
@Slf4j
public class DepartmentUIController {
    static final String DEPARTMENTS_URL = "/departments";

    private final DepartmentService service;
    private final UniqueDepartmentNameValidator nameValidator;

    @InitBinder("department")
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(nameValidator);
    }

    @GetMapping
    public String getAll(Model model) {
        log.info("getAll");
        model.addAttribute("departments", service.getAll());
        return "departments/departments";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        log.info("show department add form");
        model.addAttribute("department", new Department());
        return "departments/department-form";
    }

    @PostMapping("/create")
    public String create(@Valid Department department, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "departments/department-form";
        }
        log.info("create {}", department);
        checkNew(department);
        service.create(department);
        redirectAttributes.addFlashAttribute("action", "Department " + department.getName() + " was created");
        return "redirect:/departments";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable long id, Model model) {
        log.info("show department={} edit form", id);
        model.addAttribute("department", service.get(id));
        return "departments/department-form";
    }

    @PostMapping("/update")
    public String update(@Valid Department department, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "departments/department-form";
        }
        log.info("update {}", department);
        service.update(department);
        redirectAttributes.addFlashAttribute("action", "Department " + department.getName() + " was updated");
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
