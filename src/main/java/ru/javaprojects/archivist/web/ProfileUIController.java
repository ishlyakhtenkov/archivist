package ru.javaprojects.archivist.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.javaprojects.archivist.AuthUser;
import ru.javaprojects.archivist.service.UserService;

@Controller
@RequestMapping(value = ProfileUIController.PROFILE_URL)
@AllArgsConstructor
@Slf4j

public class ProfileUIController {
    static final String PROFILE_URL = "/profile";

    private final UserService service;

    @GetMapping
    public String profile(@AuthenticationPrincipal AuthUser authUser, Model model) {
        model.addAttribute("user", service.get(authUser.id()));
        return "profile";
    }


}
