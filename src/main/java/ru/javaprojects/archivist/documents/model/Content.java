package ru.javaprojects.archivist.documents.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import ru.javaprojects.archivist.common.HasId;
import ru.javaprojects.archivist.common.model.BaseEntity;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "document_contents", uniqueConstraints = @UniqueConstraint(columnNames = {"document_id", "change_number"}, name = "document_contents_unique_document_change_number_idx"))
@Getter
@Setter
@NoArgsConstructor
public class Content extends BaseEntity implements HasId {

    @NotNull
    @PositiveOrZero
    @Column(name = "change_number", nullable = false)
    private Integer changeNumber;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "created", columnDefinition = "timestamp default now()")
    private LocalDateTime created = LocalDateTime.now();

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;

    @Valid
    @NotEmpty
    @CollectionTable(name = "document_content_files", joinColumns = @JoinColumn(name = "document_content_id"))
    @ElementCollection(fetch = FetchType.EAGER)
    @BatchSize(size = 200)
    @JoinColumn(name = "document_content_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private List<ContentFile> files;

    public Content(Long id, Integer changeNumber, Document document, List<ContentFile> files) {
        super(id);
        this.changeNumber = changeNumber;
        this.document = document;
        this.files = files;
    }

    public Content(Long id, Integer changeNumber, LocalDateTime created, Document document, List<ContentFile> files) {
        super(id);
        this.changeNumber = changeNumber;
        this.created = created;
        this.document = document;
        this.files = files;
    }
}
