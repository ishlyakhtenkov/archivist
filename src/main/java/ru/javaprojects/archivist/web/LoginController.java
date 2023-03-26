package ru.javaprojects.archivist.web;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import ru.javaprojects.archivist.AuthUser;

@Controller
public class LoginController {
    @GetMapping("/login")
    public String login(@AuthenticationPrincipal AuthUser authUser) {
        if (authUser == null) {
            return "login";
        }
        return "redirect:/";
    }
}
