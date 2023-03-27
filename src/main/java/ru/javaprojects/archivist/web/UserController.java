package ru.javaprojects.archivist.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.javaprojects.archivist.repository.UserRepository;

@Controller
@RequestMapping("/users")
@AllArgsConstructor
@Slf4j
public class UserController {
    private final UserRepository repository;

    @GetMapping
    public String getPage(@RequestParam(value = "keyword", required = false) String keyword,
                          @PageableDefault Pageable pageable, Model model) {
        if (keyword != null) {
            if (keyword.isBlank()) {
                return "redirect:/users";
            }
            log.info("getPage(pageNumber={}, pageSize={}) with keyword={}", pageable.getPageNumber(), pageable.getPageSize(), keyword);
            model.addAttribute("usersPage",
                    repository.findAllByFirstNameContainsIgnoreCaseOrLastNameContainsIgnoreCaseOrEmailContainsIgnoreCaseOrderByLastNameAscFirstName(
                            keyword, keyword, keyword, pageable));
        } else  {
            log.info("getPage(pageNumber={}, pageSize={})", pageable.getPageNumber(), pageable.getPageSize());
            model.addAttribute("usersPage", repository.findAllByOrderByLastNameAscFirstName(pageable));
        }
        return "users";
    }
}
