package ru.javaprojects.archivist.globalsearch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(GlobalSearchUIController.SEARCH_URL)
@RequiredArgsConstructor
@Slf4j
public class GlobalSearchUIController {
    static final String SEARCH_URL = "/search";

    private final GlobalSearchService service;

    @Value("${search-results-limit}")
    private Integer searchResultsLimit;

    @GetMapping
    public String getSearchResults(@RequestParam String gsKeyword, Model model) {
        log.info("get search results by keyword={}", gsKeyword);
        if (gsKeyword.isBlank()) {
            return "redirect:/";
        }
        model.addAttribute("searchResults", service.getSearchResults(gsKeyword.trim()));
        model.addAttribute("searchResultsLimit", searchResultsLimit);
        return "global-search/search-results";
    }
}
