package ru.javaprojects.archivist.companies.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.javaprojects.archivist.common.HasId;
import ru.javaprojects.archivist.common.model.BaseEntity;
import ru.javaprojects.archivist.common.util.validation.NoHtml;

import java.util.List;
import java.util.Set;

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

    @Embedded
    @Valid
    private Contacts contacts;

    @Valid
    @CollectionTable(name = "contact_persons", joinColumns = @JoinColumn(name = "company_id"))
    @ElementCollection(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<ContactPerson> contactPersons;

    public Company(Long id, String name, Address address, Contacts contacts, List<ContactPerson> contactPersons) {
        super(id);
        this.name = name;
        this.address = address;
        this.contacts = contacts;
        this.contactPersons = contactPersons;
    }

    @Override
    public String toString() {
        return "Company:" + id + '[' + name + ']';
    }
}
