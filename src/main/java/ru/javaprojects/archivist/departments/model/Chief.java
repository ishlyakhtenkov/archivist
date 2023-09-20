package ru.javaprojects.archivist.departments.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import ru.javaprojects.archivist.common.model.Person;
import ru.javaprojects.archivist.common.util.validation.NoHtml;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@Embeddable
public class Chief extends Person {

    @Email
    @NoHtml
    @Size(max = 128)
    @Nullable
    @Column(name = "email")
    private String email;

    public Chief(String lastName, String firstName, String middleName, String phone, String email) {
        super(lastName, firstName, middleName, phone);
        this.email = email;
    }
}
