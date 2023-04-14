package ru.javaprojects.archivist.companies.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.javaprojects.archivist.common.util.validation.NoHtml;

@Data
@Embeddable
public class ContactPerson {

    @NotBlank
    @NoHtml
    @Size(min = 5, max = 64)
    @Column(name = "position", nullable = false)
    private String position;

    @NotBlank
    @NoHtml
    @Size(min = 2, max = 32)
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank
    @NoHtml
    @Size(min = 2, max = 32)
    @Column(name = "middle_name", nullable = false)
    private String middleName;

    @NotBlank
    @NoHtml
    @Size(min = 2, max = 32)
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @NoHtml
    @Size(max = 32)
    @Nullable
    @Column(name = "phone")
    private String phone;
}
