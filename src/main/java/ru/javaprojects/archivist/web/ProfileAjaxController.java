package ru.javaprojects.archivist.web;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.javaprojects.archivist.AuthUser;
import ru.javaprojects.archivist.service.UserService;

@RestController
@RequestMapping(value = ProfileUIController.PROFILE_URL, produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
@Validated
public class ProfileAjaxController {
    private final UserService service;

    @PatchMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@RequestParam @Size(min = 5, max = 32) String password, @AuthenticationPrincipal AuthUser authUser) {
        long id = authUser.id();
        log.info("change password for {}", id);
        service.changePassword(id, password);
    }
}
