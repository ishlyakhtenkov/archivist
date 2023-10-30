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
import ru.javaprojects.archivist.common.model.BaseEntity;
import ru.javaprojects.archivist.common.util.validation.NoHtml;
import ru.javaprojects.archivist.common.util.validation.NotAutoGenerated;
import ru.javaprojects.archivist.companies.model.Company;
import ru.javaprojects.archivist.departments.Department;

import java.time.LocalDate;

@Entity
@Table(name = "documents", uniqueConstraints = {@UniqueConstraint(columnNames = "decimal_number", name = "documents_unique_decimal_number_idx"),
                                                @UniqueConstraint(columnNames = "inventory_number", name = "documents_unique_inventory_number_idx")})
@Getter
@Setter
@NoArgsConstructor
public class Document extends BaseEntity implements HasId {

    @NotBlank(groups = NotAutoGenerated.class)
    @NoHtml
    @Size(min = 2, max = 128)
    @Column(name = "name")
    protected String name;

    @NotBlank
    @NoHtml
    @Size(max = 32)
    @Column(name = "decimal_number", nullable = false, unique = true)
    private String decimalNumber;

    @NotBlank(groups = NotAutoGenerated.class)
    @NoHtml
    @Size(max = 32)
    @Column(name = "inventory_number", unique = true)
    private String inventoryNumber;

    @NotNull(groups = NotAutoGenerated.class)
    @Column(name = "accounting_date")
    private LocalDate accountingDate;

    @NotNull(groups = NotAutoGenerated.class)
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private Status status;

    @Nullable
    @Enumerated(EnumType.STRING)
    @Column(name = "symbol")
    private Symbol symbol;

    @NotNull(groups = NotAutoGenerated.class)
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private Type type;

    @Column(name = "annulled", nullable = false, columnDefinition = "bool default false")
    private boolean annulled = false;

    @Column(name = "auto_generated", nullable = false, columnDefinition = "bool default false")
    private boolean autoGenerated = false;

    @NoHtml
    @Size(max = 128)
    @Nullable
    @Column(name = "comment")
    private String comment;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "developer_id")
    private Department developer;

    @NotNull(groups = NotAutoGenerated.class)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_holder_id")
    private Company originalHolder;

    public Document(Long id, String name, String decimalNumber, String inventoryNumber, LocalDate accountingDate,
                    Status status, Symbol symbol, Type type, boolean annulled, String comment, Department developer,
                    Company originalHolder) {
        super(id);
        this.name = name;
        this.decimalNumber = decimalNumber;
        this.inventoryNumber = inventoryNumber;
        this.accountingDate = accountingDate;
        this.status = status;
        this.symbol = symbol;
        this.type = type;
        this.annulled = annulled;
        this.comment = comment;
        this.developer = developer;
        this.originalHolder = originalHolder;
    }

    public static Document autoGenerate(String decimalNumber) {
        Document document = new Document();
        document.setDecimalNumber(decimalNumber);
        document.setAutoGenerated(true);
        return document;
    }

    @Override
    public String toString() {
        return super.toString() + '[' + decimalNumber + ']';
    }
}
