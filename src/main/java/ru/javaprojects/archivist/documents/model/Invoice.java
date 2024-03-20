package ru.javaprojects.archivist.documents.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.archivist.common.HasId;
import ru.javaprojects.archivist.common.model.BaseEntity;
import ru.javaprojects.archivist.common.util.validation.NoHtml;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "invoices", uniqueConstraints = @UniqueConstraint(columnNames = {"number", "date"}, name = "invoices_unique_number_date_idx"))
@Getter
@Setter
@NoArgsConstructor
public class Invoice extends BaseEntity implements HasId {

    @NotBlank
    @NoHtml
    @Size(max = 10)
    @Column(name = "number", unique = true)
    private String number;

    @NotNull
    @Column(name = "date")
    private LocalDate date;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "doc_status")
    private Status status;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "letter_id", nullable = false)
    private Letter letter;

    @OneToMany(mappedBy = "invoice", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Sending> sendings;

    public Invoice(Long id, String number, LocalDate date, Status status, Letter letter) {
        super(id);
        this.number = number;
        this.date = date;
        this.status = status;
        this.letter = letter;
    }
}
