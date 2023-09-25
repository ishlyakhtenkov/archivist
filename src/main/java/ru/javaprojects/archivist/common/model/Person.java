package ru.javaprojects.archivist.common.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.javaprojects.archivist.common.util.validation.NoHtml;

@MappedSuperclass
@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Person {
    @NotBlank
    @NoHtml
    @Size(min = 2, max = 32)
    @Column(name = "last_name", nullable = false)
    protected String lastName;

    @NotBlank
    @NoHtml
    @Size(min = 2, max = 32)
    @Column(name = "first_name", nullable = false)
    protected String firstName;

    @NotBlank
    @NoHtml
    @Size(min = 2, max = 32)
    @Column(name = "middle_name", nullable = false)
    protected String middleName;

    @NoHtml
    @Size(max = 32)
    @Nullable
    @Column(name = "phone")
    protected String phone;

    @Email
    @NoHtml
    @Size(max = 128)
    @Nullable
    @Column(name = "email")
    protected String email;

    public String getFullName() {
        return String.join(" ", lastName, firstName, middleName);
    }
}
