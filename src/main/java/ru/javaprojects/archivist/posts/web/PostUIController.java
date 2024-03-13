package ru.javaprojects.archivist.posts.web;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.javaprojects.archivist.posts.PostService;
import ru.javaprojects.archivist.posts.PostTo;

import static ru.javaprojects.archivist.posts.PostUtil.asTo;

@Controller
@RequestMapping(PostUIController.POSTS_URL)
@AllArgsConstructor
@Slf4j
public class PostUIController {
    static final String POSTS_URL = "/posts";

    private final PostService service;

    @GetMapping("/add")
    public String showAddForm(Model model) {
        log.info("show post add form");
        model.addAttribute("postTo", new PostTo());
        return "posts/post-form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable long id, Model model) {
        log.info("show edit form for post with id={}", id);
        model.addAttribute("postTo", asTo(service.get(id)));
        return "posts/post-form";
    }

    @PostMapping
    public String createOrUpdate(@Valid PostTo postTo, BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "posts/post-form";
        }
        boolean isNew = postTo.isNew();
        log.info((isNew ? "create" : "update") + " {}", postTo);
        if (isNew) {
            service.create(postTo);
        } else {
            service.update(postTo);
        }
        redirectAttributes.addFlashAttribute("action", "Post " + postTo.getTitle() +
                (isNew ? " was created" : " was updated"));
        return "redirect:/";
    }
}
