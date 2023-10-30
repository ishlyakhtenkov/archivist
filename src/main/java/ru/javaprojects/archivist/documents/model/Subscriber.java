package ru.javaprojects.archivist.documents.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.archivist.common.HasId;
import ru.javaprojects.archivist.common.model.BaseEntity;
import ru.javaprojects.archivist.companies.model.Company;

@Entity
@Table(name = "subscribers", uniqueConstraints = @UniqueConstraint(columnNames = {"document_id", "company_id"}, name = "subscribers_unique_document_company_idx"))
@Getter
@Setter
@NoArgsConstructor
public class Subscriber extends BaseEntity implements HasId {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @Column(name = "accounted", nullable = false, columnDefinition = "bool default true")
    private boolean accounted = true;
}
