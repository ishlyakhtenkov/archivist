package ru.javaprojects.archivist.companies.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.javaprojects.archivist.common.HasId;
import ru.javaprojects.archivist.common.model.NamedEntity;

import java.util.List;

@Entity
@Table(name = "companies", uniqueConstraints = {@UniqueConstraint(columnNames = "name", name = "companies_unique_name_idx")})
@Getter
@Setter
@NoArgsConstructor
public class Company extends NamedEntity implements HasId {

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
        super(id, name);
        this.name = name;
        this.address = address;
        this.contacts = contacts;
        this.contactPersons = contactPersons;
    }
}
