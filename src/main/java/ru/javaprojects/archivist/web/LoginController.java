package ru.javaprojects.archivist.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.javaprojects.archivist.AuthUser;

@Controller
@RequestMapping(LoginController.LOGIN_URL)
public class LoginController {
    static final String LOGIN_URL = "/login";

    @GetMapping
    public String login(@AuthenticationPrincipal AuthUser authUser) {
        if (authUser == null) {
            return "login";
        }
        return "redirect:/";
    }
}
