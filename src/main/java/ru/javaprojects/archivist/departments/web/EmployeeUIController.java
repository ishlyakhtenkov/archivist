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
import ru.javaprojects.archivist.departments.model.Employee;
import ru.javaprojects.archivist.departments.service.DepartmentService;
import ru.javaprojects.archivist.departments.service.EmployeeService;

@Controller
@RequestMapping(EmployeeUIController.EMPLOYEES_URL)
@AllArgsConstructor
@Slf4j
public class EmployeeUIController {
    static final String EMPLOYEES_URL = "/employees";

    private final EmployeeService service;
    private final DepartmentService departmentService;
    private UniqueEmployeeEmailValidator emailValidator;

    @InitBinder("employee")
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(emailValidator);
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping("/add")
    public String showAddForm(@RequestParam String chosenDepartmentId, Model model) {
        log.info("show employee add form");
        model.addAttribute("employee", new Employee());
        model.addAttribute("departments", departmentService.getAllWithBoss());
        model.addAttribute("chosenDepartmentId", chosenDepartmentId);
        return "departments/employees/employee-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable long id, Model model) {
        log.info("show edit form for employee with id={}", id);
        model.addAttribute("employee", service.getWithDepartment(id));
        model.addAttribute("departments", departmentService.getAllWithBoss());
        return "departments/employees/employee-form";
    }

    @PostMapping
    public String createOrUpdate(@Valid Employee employee, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("departments", departmentService.getAllWithBoss());
            model.addAttribute("chosenDepartmentId", employee.getDepartment().getId());
            return "departments/employees/employee-form";
        }
        boolean isNew = employee.isNew();
        log.info((isNew ? "create" : "update") + " {}", employee);
        if (isNew) {
            service.create(employee);
        } else {
            service.update(employee);
        }
        redirectAttributes.addFlashAttribute("action", "Employee " + employee.getFullName() +
                (isNew ? " was created" : " was updated"));
        return "redirect:/departments/edit/" + employee.getDepartment().getId();
    }
}
