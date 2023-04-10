package ru.javaprojects.archivist.companies.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.javaprojects.archivist.common.HasId;
import ru.javaprojects.archivist.common.model.BaseEntity;
import ru.javaprojects.archivist.common.util.validation.NoHtml;

import java.util.List;

@Entity
@Table(name = "companies", uniqueConstraints = {@UniqueConstraint(columnNames = "name", name = "companies_unique_name_idx")})
@Getter
@Setter
@NoArgsConstructor
public class Company extends BaseEntity implements HasId {

    @NotBlank
    @NoHtml
    @Size(min = 2, max = 128)
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Embedded
    @Valid
    private Address address;

    @Valid
    @CollectionTable(name = "contact_persons", joinColumns = @JoinColumn(name = "company_id"),
            uniqueConstraints = {@UniqueConstraint(columnNames = {"company_id", "position"},
                    name = "contact_persons_unique_company_position_idx")})
    @ElementCollection(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<ContactPerson> contactPersons;

    @Override
    public String toString() {
        return "Company:" + id + '[' + name + ']';
    }
}
