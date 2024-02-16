package ru.javaprojects.archivist.references.employees;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.archivist.common.HasId;
import ru.javaprojects.archivist.common.model.BaseEntity;
import ru.javaprojects.archivist.common.util.validation.NoHtml;
import ru.javaprojects.archivist.departments.Department;


@Entity
@Table(name = "employees", uniqueConstraints = {@UniqueConstraint(columnNames = "email", name = "employees_unique_email_idx")})
@Getter
@Setter
@NoArgsConstructor
public class Employee extends BaseEntity implements HasId {
    @NotBlank
    @NoHtml
    @Size(min = 2, max = 32)
    @Column(name = "last_name", nullable = false)
    private String lastName;

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
    @Size(max = 32)
    @Column(name = "phone")
    private String phone;

    @Email
    @NoHtml
    @Size(max = 128)
    @Nullable
    @Column(name = "email")
    private String email;

    @Column(name = "fired", nullable = false, columnDefinition = "bool default false")
    private boolean fired = false;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    public String getFullName() {
        return String.join(" ", lastName, firstName, middleName);
    }
}
