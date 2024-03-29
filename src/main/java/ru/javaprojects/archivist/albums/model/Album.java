package ru.javaprojects.archivist.albums.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.archivist.common.HasId;
import ru.javaprojects.archivist.common.model.BaseEntity;
import ru.javaprojects.archivist.documents.model.Document;

import java.util.List;

@Entity
@Table(name = "albums", uniqueConstraints = {@UniqueConstraint(columnNames = {"document_id", "stamp"}, name = "albums_unique_document_stamp_idx")})
@Getter
@Setter
@NoArgsConstructor
public class Album extends BaseEntity implements HasId {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    private Document mainDocument;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "stamp")
    private Stamp stamp;

    @OneToMany(mappedBy = "album", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<Issuance> issuances;

    public Album(Long id, Document mainDocument, Stamp stamp) {
        super(id);
        this.mainDocument = mainDocument;
        this.stamp = stamp;
    }
}
