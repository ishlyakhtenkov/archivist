package ru.javaprojects.archivist.web;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.javaprojects.archivist.model.Role;
import ru.javaprojects.archivist.model.User;
import ru.javaprojects.archivist.service.UserService;
import ru.javaprojects.archivist.to.UserTo;

import static ru.javaprojects.archivist.util.UserUtil.asTo;
import static ru.javaprojects.archivist.util.validation.ValidationUtil.checkNew;

@Controller
@RequestMapping("/users")
@AllArgsConstructor
@Slf4j
@Validated
public class AdminUserController {
    private final UserService service;
    private UniqueMailValidator emailValidator;

    @InitBinder({"user", "userTo"})
    protected void initBinder(WebDataBinder binder) {
        binder.addValidators(emailValidator);
    }

    @GetMapping
    public String getPage(@RequestParam(value = "keyword", required = false) String keyword,
                          @PageableDefault Pageable pageable, Model model) {
        Page<User> usersPage;
        if (keyword != null) {
            if (keyword.isBlank()) {
                return "redirect:/users";
            }
            log.info("getPage(pageNumber={}, pageSize={}) with keyword={}", pageable.getPageNumber(), pageable.getPageSize(), keyword);
            usersPage = service.getAllByKeyword(pageable, keyword.trim());
        } else  {
            log.info("getPage(pageNumber={}, pageSize={})", pageable.getPageNumber(), pageable.getPageSize());
            usersPage = service.getAll(pageable);
        }
        model.addAttribute("usersPage", usersPage);
        return "users";
    }

    @GetMapping("/add")
    public String showAddForm(Model model) {
        model.addAttribute("roles", Role.values());
        model.addAttribute("user", new User());
        return "user-add";
    }

    @PostMapping("/add")
    public String create(@Validated User user, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("roles", Role.values());
            return "user-add";
        }
        log.info("create {}", user);
        checkNew(user);
        service.create(user);
        redirectAttributes.addFlashAttribute("userCreated", user.getFirstName() + " " + user.getLastName());
        return "redirect:/users";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable long id, Model model) {
        model.addAttribute("roles", Role.values());
        model.addAttribute("userTo", asTo(service.get(id)));
        return "user-edit";
    }

    @PostMapping("/update")
    public String update(@Validated UserTo userTo, BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("roles", Role.values());
            return "user-edit";
        }
        log.info("update {}", userTo);
        service.update(userTo);
        redirectAttributes.addFlashAttribute("userUpdated", userTo.getFirstName() + " " + userTo.getLastName());
        return "redirect:/users";
    }

    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enable(@PathVariable long id, @RequestParam boolean enabled) {
        log.info(enabled ? "enable {}" : "disable {}", id);
        service.enable(id, enabled);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        log.info("delete {}", id);
        service.delete(id);
    }

    @PatchMapping("/{id}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@PathVariable long id, @RequestParam @Size(min = 5, max = 32) String password) {
        log.info("change password for {}", id);
        service.changePassword(id, password);
    }
}
