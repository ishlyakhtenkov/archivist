package ru.javaprojects.archivist.users;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.javaprojects.archivist.common.model.BaseEntity;
import ru.javaprojects.archivist.common.util.validation.NoHtml;

import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "users", uniqueConstraints = {@UniqueConstraint(columnNames = "email", name = "users_unique_email_idx")})
@Getter
@Setter
@NoArgsConstructor
public class User extends BaseEntity implements HasIdAndEmail {

    @Email
    @NotBlank
    @NoHtml
    @Size(max = 128)
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Size(min = 5, max = 128)
    @Column(name = "password", nullable = false)
    private String password;

    @NotBlank
    @NoHtml
    @Size(min = 2, max = 32)
    @Column(name = "first_name", nullable = false)
    private String firstName;

    @NotBlank
    @NoHtml
    @Size(min = 2, max = 32)
    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "enabled", nullable = false, columnDefinition = "bool default true")
    private boolean enabled = true;

    @NotNull
    @Column(name = "registered", nullable = false, columnDefinition = "timestamp default now()")
    private Date registered = new Date();

    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "role"}, name = "user_roles_unique_idx")})
    @Column(name = "role")
    @ElementCollection(fetch = FetchType.EAGER)
    @BatchSize(size = 200)
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotEmpty
    private Set<Role> roles;

    public User(Long id, String email, String firstName, String lastName, boolean enabled, Set<Role> roles) {
        super(id);
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.enabled = enabled;
        this.roles = roles;
    }

    public User(Long id, String email, String password, String firstName, String lastName, boolean enabled, Set<Role> roles) {
        this(id, email, firstName, lastName, enabled, roles);
        this.password = password;
    }

    public String getFullName() {
        return lastName + " " + firstName;
    }

    @Override
    public String toString() {
        return "User:" + id + '[' + email + ']';
    }
}
