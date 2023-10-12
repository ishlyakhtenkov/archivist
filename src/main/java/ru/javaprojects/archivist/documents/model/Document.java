package ru.javaprojects.archivist.documents.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.archivist.common.HasId;
import ru.javaprojects.archivist.common.model.NamedEntity;
import ru.javaprojects.archivist.common.util.validation.NoHtml;
import ru.javaprojects.archivist.companies.model.Company;
import ru.javaprojects.archivist.departments.Department;

import java.time.LocalDate;

@Entity
@Table(name = "documents", uniqueConstraints = {@UniqueConstraint(columnNames = "decimal_number", name = "documents_unique_decimal_number_idx"),
                                                @UniqueConstraint(columnNames = "inventory_number", name = "documents_unique_inventory_number_idx")})
@Getter
@Setter
@NoArgsConstructor
public class Document extends NamedEntity implements HasId {
    @NotBlank
    @NoHtml
    @Size(max = 32)
    @Column(name = "decimal_number", nullable = false, unique = true)
    private String decimalNumber;

    @NotBlank
    @NoHtml
    @Size(max = 32)
    @Column(name = "inventory_number", nullable = false, unique = true)
    private String inventoryNumber;

    @NotNull
    @Column(name = "accounting_date", nullable = false)
    private LocalDate accountingDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(name = "letter")
    private Letter letter;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private Type type;

    @Column(name = "annulled", nullable = false, columnDefinition = "bool default false")
    private boolean annulled = false;

    @NoHtml
    @Size(max = 128)
    @Nullable
    @Column(name = "comment")
    private String comment;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "developer_id")
    private Department developer;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_holder_id", nullable = false)
    private Company originalHolder;
}
