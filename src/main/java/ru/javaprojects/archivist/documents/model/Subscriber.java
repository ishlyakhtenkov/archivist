package ru.javaprojects.archivist.documents.model;

import com.fasterxml.jackson.annotation.JsonProperty;
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

import java.time.LocalDateTime;

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

    @Column(name = "subscribed", nullable = false, columnDefinition = "bool default true")
    private boolean subscribed = true;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "doc_status")
    private Status status;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "unsubscribe_timestamp")
    private LocalDateTime unsubscribeTimestamp;

    @NoHtml
    @Size(max = 256)
    @Column(name = "unsubscribe_reason")
    private String unsubscribeReason;
}
