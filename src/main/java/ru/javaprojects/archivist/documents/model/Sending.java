package ru.javaprojects.archivist.documents.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.archivist.common.HasId;
import ru.javaprojects.archivist.common.model.BaseEntity;

@Entity
@Table(name = "sendings", uniqueConstraints = @UniqueConstraint(columnNames = {"document_id", "invoice_id"}, name = "sendings_unique_document_invoice_idx"))
@Getter
@Setter
@NoArgsConstructor
public class Sending extends BaseEntity implements HasId {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    private Invoice invoice;

    public Sending(Long id, Document document, Invoice invoice) {
        super(id);
        this.document = document;
        this.invoice = invoice;
    }
}
