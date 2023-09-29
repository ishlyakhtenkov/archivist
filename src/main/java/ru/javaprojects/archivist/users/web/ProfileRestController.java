package ru.javaprojects.archivist.users.web;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.javaprojects.archivist.ArchivistApplication;
import ru.javaprojects.archivist.users.UserService;
import ru.javaprojects.archivist.users.password_reset.PasswordResetService;

@RestController
@RequestMapping(value = ProfileUIController.PROFILE_URL, produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
@Slf4j
@Validated
public class ProfileRestController {
    private final UserService userService;
    private final PasswordResetService passwordResetService;

    @PatchMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@RequestParam @Size(min = 5, max = 32) String password, @AuthenticationPrincipal ArchivistApplication.AuthUser authUser) {
        long id = authUser.id();
        log.info("change password for {}", id);
        userService.changePassword(id, password);
    }

    @PostMapping("/forgotPassword")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void forgotPassword(@RequestParam String email) {
        log.info("forgot password {}", email);
        passwordResetService.sendPasswordResetEmail(email);
    }
}
