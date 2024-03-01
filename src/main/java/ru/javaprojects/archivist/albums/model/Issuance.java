package ru.javaprojects.archivist.albums.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.archivist.common.HasId;
import ru.javaprojects.archivist.common.model.BaseEntity;
import ru.javaprojects.archivist.departments.model.Employee;

import java.time.LocalDate;

@Entity
@Table(name = "issuances")
@Getter
@Setter
@NoArgsConstructor
public class Issuance extends BaseEntity implements HasId {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", nullable = false)
    @JsonBackReference
    private Album album;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @NotNull
    @Column(name = "issued")
    private LocalDate issued;

    @Column(name = "returned")
    private LocalDate returned;

    public Issuance(Long id, Album album, Employee employee, LocalDate issued) {
        super(id);
        this.album = album;
        this.employee = employee;
        this.issued = issued;
    }

    public Issuance(Long id, Album album, Employee employee, LocalDate issued, LocalDate returned) {
        this(id, album, employee, issued);
        this.returned = returned;
    }
}
