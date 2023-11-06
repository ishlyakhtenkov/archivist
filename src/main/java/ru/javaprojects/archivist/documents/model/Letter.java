package ru.javaprojects.archivist.documents.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.archivist.common.HasId;
import ru.javaprojects.archivist.common.model.BaseEntity;
import ru.javaprojects.archivist.common.util.validation.NoHtml;
import ru.javaprojects.archivist.companies.model.Company;

import java.time.LocalDate;

@Entity
@Table(name = "letters", uniqueConstraints = @UniqueConstraint(columnNames = "number", name = "letters_unique_number_idx"))
@Getter
@Setter
@NoArgsConstructor
public class Letter extends BaseEntity implements HasId {

    @NoHtml
    @Size(max = 16)
    @Column(name = "number", unique = true)
    private String number;

    @Column(name = "date")
    private LocalDate date;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    public Letter(Long id, String number, LocalDate date, Company company) {
        super(id);
        this.number = number;
        this.date = date;
        this.company = company;
    }
}
