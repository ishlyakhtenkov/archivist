package ru.javaprojects.archivist.users.web;

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
import ru.javaprojects.archivist.users.*;

import static ru.javaprojects.archivist.common.util.validation.ValidationUtil.checkNew;
import static ru.javaprojects.archivist.common.util.validation.ValidationUtil.checkNotNew;
import static ru.javaprojects.archivist.users.UserUtil.asTo;

@Controller
@RequestMapping(AdminUserUIController.USERS_URL)
@AllArgsConstructor
@Slf4j
public class AdminUserUIController {
    static final String USERS_URL = "/users";

    private final UserService service;
    private UniqueMailValidator emailValidator;

    @InitBinder({"user", "userTo"})
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(emailValidator);
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @GetMapping
    public String getAll(@RequestParam(value = "keyword", required = false) String keyword,
                            @PageableDefault Pageable pageable, Model model, RedirectAttributes redirectAttributes) {
        Page<User> users;
        if (keyword != null) {
            if (keyword.isBlank()) {
                return "redirect:/users";
            }
            log.info("getAll(pageNumber={}, pageSize={}, keyword={})", pageable.getPageNumber(), pageable.getPageSize(), keyword);
            users = service.getAll(pageable, keyword.trim());
        } else  {
            log.info("getAll(pageNumber={}, pageSize={})", pageable.getPageNumber(), pageable.getPageSize());
            users = service.getAll(pageable);
        }
        if (users.getContent().isEmpty() && users.getTotalElements() != 0) {
            if (keyword != null) {
                redirectAttributes.addAttribute("keyword", keyword);
            }
            return "redirect:/users";
        }
        model.addAttribute("users", users);
        return "users/users";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        log.info("show user add form");
        model.addAttribute("roles", Role.values());
        model.addAttribute("user", new User());
        return "users/user-add-form";
    }

    @PostMapping("/create")
    public String create(@Valid User user, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("roles", Role.values());
            return "users/user-add-form";
        }
        log.info("create {}", user);
        checkNew(user);
        service.create(user);
        redirectAttributes.addFlashAttribute("action", "User " + user.getFullName() + " was created");
        return "redirect:/users";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable long id, Model model) {
        log.info("show user={} edit form", id);
        model.addAttribute("roles", Role.values());
        model.addAttribute("userTo", asTo(service.get(id)));
        return "users/user-edit-form";
    }

    @PostMapping("/update")
    public String update(@Valid UserTo userTo, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("roles", Role.values());
            return "users/user-edit-form";
        }
        log.info("update {}", userTo);
        checkNotNew(userTo);
        service.update(userTo);
        redirectAttributes.addFlashAttribute("action", "User " + userTo.getFullName() + " was updated");
        return "redirect:/users";
    }
}
