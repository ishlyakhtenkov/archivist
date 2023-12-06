package ru.javaprojects.archivist.common.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.javaprojects.archivist.posts.Post;
import ru.javaprojects.archivist.posts.PostService;
import ru.javaprojects.archivist.users.AuthUser;

@Controller
@RequestMapping
@AllArgsConstructor
@Slf4j
public class PathUIController {
    public static final String LOGIN_URL = "/login";

    private final PostService service;

    @GetMapping
    public String showHomePage(@PageableDefault Pageable pageable, Model model) {
        log.info("show home page(pageNumber={}, pageSize={})", pageable.getPageNumber(), pageable.getPageSize());
        Page<Post> posts = service.getAll(pageable, AuthUser.safeGet() != null);
        if (posts.getContent().isEmpty() && posts.getTotalElements() != 0) {
            return "redirect:/";
        }
        model.addAttribute("posts", posts);
        return "index";
    }

    @GetMapping(LOGIN_URL)
    public String showLoginPage(@AuthenticationPrincipal AuthUser authUser) {
        log.info("show login page for user {}", authUser);
        if (authUser == null) {
            return "users/login";
        }
        return "redirect:/";
    }
}
