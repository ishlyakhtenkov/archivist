package ru.javaprojects.archivist.departments;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.archivist.common.HasId;
import ru.javaprojects.archivist.common.model.NamedEntity;
import ru.javaprojects.archivist.common.model.Person;

@Entity
@Table(name = "departments", uniqueConstraints = {@UniqueConstraint(columnNames = "name", name = "departments_unique_name_idx")})
@Getter
@Setter
@NoArgsConstructor
public class Department extends NamedEntity implements HasId {

    @Embedded
    @Valid
    private Person boss;

    public Department(Long id, String name, Person boss) {
        super(id, name);
        this.boss = boss;
    }
}
