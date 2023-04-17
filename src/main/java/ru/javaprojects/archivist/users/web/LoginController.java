package ru.javaprojects.archivist.users.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.javaprojects.archivist.ArchivistApplication;

@Controller
@RequestMapping(LoginController.LOGIN_URL)
public class LoginController {
    public static final String LOGIN_URL = "/login";

    @GetMapping
    public String login(@AuthenticationPrincipal ArchivistApplication.AuthUser authUser) {
        if (authUser == null) {
            return "users/login";
        }
        return "redirect:/";
    }
}
