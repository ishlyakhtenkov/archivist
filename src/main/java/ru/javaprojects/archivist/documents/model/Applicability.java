package ru.javaprojects.archivist.documents.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.archivist.common.HasId;
import ru.javaprojects.archivist.common.model.BaseEntity;

@Entity
@Table(name = "applicabilities", uniqueConstraints = @UniqueConstraint(columnNames = {"document_id", "applicability_id"},
        name = "applicabilities_unique_document_applicability_idx"))
@Getter
@Setter
@NoArgsConstructor
public class Applicability extends BaseEntity implements HasId {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicability_id")
    private Document applicability;

    @Column(name = "primal", nullable = false, columnDefinition = "bool default false")
    private boolean primal = false;
}
