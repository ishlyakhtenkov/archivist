package ru.javaprojects.archivist.changenotices.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.archivist.common.HasId;
import ru.javaprojects.archivist.common.model.BaseEntity;
import ru.javaprojects.archivist.documents.model.Document;

@Entity
@Table(name = "changes", uniqueConstraints = {@UniqueConstraint(columnNames = {"document_id", "change_notice_id"}, name = "changes_unique_document_change_notice_idx"),
         @UniqueConstraint(columnNames = {"document_id", "change_number"}, name = "changes_unique_document_change_number_idx")})
@Getter
@Setter
@NoArgsConstructor
public class Change extends BaseEntity implements HasId {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document document;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "change_notice_id", nullable = false)
    private ChangeNotice changeNotice;

    @NotNull
    @PositiveOrZero
    @Column(name = "change_number", nullable = false)
    private Integer changeNumber;

    public Change(Long id, Document document, ChangeNotice changeNotice, Integer changeNumber) {
        super(id);
        this.document = document;
        this.changeNotice = changeNotice;
        this.changeNumber = changeNumber;
    }
}
