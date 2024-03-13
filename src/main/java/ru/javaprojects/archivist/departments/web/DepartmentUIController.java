package ru.javaprojects.archivist.departments.web;

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
import ru.javaprojects.archivist.departments.service.EmployeeService;
import ru.javaprojects.archivist.departments.to.DepartmentCreateTo;
import ru.javaprojects.archivist.departments.to.DepartmentUpdateTo;

import static ru.javaprojects.archivist.departments.DepartmentUtil.asUpdateTo;

@Controller
@RequestMapping(DepartmentUIController.DEPARTMENTS_URL)
@AllArgsConstructor
@Slf4j
public class DepartmentUIController {
    public static final String DEPARTMENTS_URL = "/departments";

    private final DepartmentService service;
    private final EmployeeService employeeService;
    private final UniqueDepartmentNameValidator nameValidator;

    @InitBinder({"departmentCreateTo", "departmentUpdateTo"})
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(nameValidator);
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping
    public String getAll(Model model) {
        log.info("get departments");
        model.addAttribute("departments", service.getAllWithBoss());
        return "departments/departments";
    }

    @GetMapping("/{id}")
    public String get(@PathVariable long id, Model model) {
        log.info("get department with id={}", id);
        model.addAttribute("department", service.getWithEmployees(id));
        return "departments/department";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        log.info("show department add form");
        model.addAttribute("departmentCreateTo", new DepartmentCreateTo());
        return "departments/department-add-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable long id, Model model) {
        log.info("show edit form for department with id={}", id);
        Department department = service.getWithEmployees(id);
        model.addAttribute("departmentUpdateTo", asUpdateTo(department));
        model.addAttribute("employees", department.getEmployees());
        return "departments/department-edit-form";
    }

    @PostMapping("/create")
    public String create(@Valid DepartmentCreateTo departmentCreateTo, BindingResult result,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "departments/department-add-form";
        }
        log.info("create {}", departmentCreateTo);
        service.create(departmentCreateTo);
        redirectAttributes.addFlashAttribute("action", "Department " + departmentCreateTo.getName() + " was created");
        return "redirect:/departments";
    }

    @PostMapping("/update")
    public String update(@Valid DepartmentUpdateTo departmentUpdateTo, BindingResult result, Model model,
                         RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("employees", employeeService.getAllByDepartment(departmentUpdateTo.getId()));
            return "departments/department-edit-form";
        }
        log.info("update {}", departmentUpdateTo);
        service.update(departmentUpdateTo);
        redirectAttributes.addFlashAttribute("action", "Department " + departmentUpdateTo.getName() + " was updated");
        return "redirect:/departments";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable long id, RedirectAttributes redirectAttributes) {
        log.info("delete department with id={}", id);
        Department department = service.get(id);
        service.delete(id);
        redirectAttributes.addFlashAttribute("action", "Department " + department.getName() + " was deleted");
        return "redirect:/departments";
    }
}
