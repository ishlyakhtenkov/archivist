package ru.javaprojects.archivist.companies.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
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
public class ContactPerson extends Person {

    @NotBlank
    @NoHtml
    @Size(min = 5, max = 64)
    @Column(name = "position", nullable = false)
    private String position;

    public ContactPerson(String position, String lastName, String firstName, String middleName, String phone) {
        super(lastName, firstName, middleName, phone);
        this.position = position;
    }
}
