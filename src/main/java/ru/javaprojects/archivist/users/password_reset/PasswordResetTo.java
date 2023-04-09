package ru.javaprojects.archivist.users.password_reset;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordResetTo {

    @Size(min = 5, max = 128)
    private String password;

    @NotBlank
    private String token;

    public PasswordResetTo(String token) {
        this.token = token;
    }
}
