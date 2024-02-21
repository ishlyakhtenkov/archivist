package ru.javaprojects.archivist.departments.model;

import jakarta.persistence.*;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.archivist.common.HasId;
import ru.javaprojects.archivist.common.model.NamedEntity;
import ru.javaprojects.archivist.common.model.Person;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "departments", uniqueConstraints = {@UniqueConstraint(columnNames = "name", name = "departments_unique_name_idx")})
@Getter
@Setter
@NoArgsConstructor
public class Department extends NamedEntity implements HasId {

    @Embedded
    @Valid
    private Person boss;

    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private List<Employee> employees;

    public Department(Long id, String name, Person boss) {
        super(id, name);
        this.boss = boss;
    }

    public void addEmployee(Employee employee) {
        if (employees == null) {
            employees = new ArrayList<>();
        }
        employees.add(employee);
        employee.setDepartment(this);
    }
}
