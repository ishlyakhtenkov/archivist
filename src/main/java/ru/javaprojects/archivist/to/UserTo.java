package ru.javaprojects.archivist.to;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.javaprojects.archivist.HasIdAndEmail;
import ru.javaprojects.archivist.model.Role;
import ru.javaprojects.archivist.util.validation.NoHtml;

import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class UserTo extends BaseTo implements HasIdAndEmail {

    @Email
    @NotBlank
    @NoHtml
    @Size(max = 128)
    private String email;

    @NotBlank
    @NoHtml
    @Size(min = 2, max = 32)
    private String firstName;

    @NotBlank
    @NoHtml
    @Size(min = 2, max = 32)
    private String lastName;

    @NotEmpty
    private Set<Role> roles;

    public UserTo(Long id, String email, String firstName, String lastName, Set<Role> roles) {
        super(id);
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.roles = roles;
    }

    @Override
    public String toString() {
        return "UserTo:" + id + '[' + email + ']';
    }
}
