package ru.javaprojects.archivist.departments.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.archivist.common.HasIdAndEmail;
import ru.javaprojects.archivist.common.model.BaseEntity;
import ru.javaprojects.archivist.common.util.validation.NoHtml;


@Entity
@Table(name = "employees", uniqueConstraints = {@UniqueConstraint(columnNames = "email", name = "employees_unique_email_idx")})
@Getter
@Setter
@NoArgsConstructor
public class Employee extends BaseEntity implements HasIdAndEmail {
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

    public String getLastNameAndInitials() {
        return lastName + " " + firstName.charAt(0) + "." + middleName.charAt(0) + ".";
    }

    public Employee(Long id, String lastName, String firstName, String middleName, String phone, String email) {
        super(id);
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleName = middleName;
        this.phone = phone;
        this.email = email;
    }

    public Employee(Long id, String lastName, String firstName, String middleName, String phone, String email, Department department) {
        this(id, lastName, firstName, middleName, phone, email);
        this.department = department;
    }

    @Override
    public String toString() {
        return String.format("Employee[id=%d, name=%s]", id, getFullName());
    }
}
