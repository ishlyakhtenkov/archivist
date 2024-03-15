package ru.javaprojects.archivist.tools.web;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping(ToolUIController.TOOLS_URL)
@AllArgsConstructor
@Slf4j
public class ToolUIController {
    static final String TOOLS_URL = "/tools";

    @GetMapping
    public String showToolsPage() {
        log.info("show tools page");
        return "tools/tools";
    }
}
