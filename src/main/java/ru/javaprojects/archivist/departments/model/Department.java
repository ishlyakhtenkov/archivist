package ru.javaprojects.archivist.departments.model;

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

@Entity
@Table(name = "departments", uniqueConstraints = {@UniqueConstraint(columnNames = "name", name = "departments_unique_name_idx")})
@Getter
@Setter
@NoArgsConstructor
public class Department extends NamedEntity implements HasId {

    @Embedded
    @Valid
    private Chief chief;
}
