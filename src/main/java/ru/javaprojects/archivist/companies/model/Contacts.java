package ru.javaprojects.archivist.companies.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.javaprojects.archivist.common.util.validation.NoHtml;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class Contacts {

    @NoHtml
    @Size(max = 32)
    @Nullable
    @Column(name = "phone")
    private String phone;

    @NoHtml
    @Size(max = 32)
    @Nullable
    @Column(name = "fax")
    private String fax;

    @Email
    @NoHtml
    @Size(max = 128)
    @Nullable
    @Column(name = "email")
    private String email;
}
