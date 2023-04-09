package ru.javaprojects.archivist.users.web;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.javaprojects.archivist.ArchivistApplication;
import ru.javaprojects.archivist.users.UserService;
import ru.javaprojects.archivist.users.password_reset.PasswordResetService;
import ru.javaprojects.archivist.users.password_reset.PasswordResetTo;

@Controller
@RequestMapping(ProfileUIController.PROFILE_URL)
@AllArgsConstructor
@Slf4j
public class ProfileUIController {
    static final String PROFILE_URL = "/profile";

    private final UserService userService;
    private final PasswordResetService passwordResetService;

    @GetMapping
    public String profile(@AuthenticationPrincipal ArchivistApplication.AuthUser authUser, Model model) {
        log.info("show profile {}", authUser.id());
        model.addAttribute("user", userService.get(authUser.id()));
        return "users/profile";
    }

    @GetMapping("/resetPassword")
    public String showResetPasswordForm(@RequestParam String token, Model model) {
        log.info("show reset password form token={}", token);
        passwordResetService.checkToken(token);
        model.addAttribute("passwordResetTo",  new PasswordResetTo(token));
        return "users/reset-password";
    }

    @PostMapping("/resetPassword")
    public String resetPassword(@Valid PasswordResetTo passwordResetTo, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "users/reset-password";
        }
        log.info("reset password token={}", passwordResetTo.getToken());
        passwordResetService.resetPassword(passwordResetTo);
        redirectAttributes.addFlashAttribute("action", "Password has been successfully reset");
        return "redirect:/login";
    }
}
