package ru.javaprojects.archivist.changenotices.model;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.archivist.common.HasId;
import ru.javaprojects.archivist.common.model.NamedEntity;
import ru.javaprojects.archivist.common.util.validation.NotAutoGenerated;
import ru.javaprojects.archivist.departments.Department;
import ru.javaprojects.archivist.documents.model.ContentFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "change_notices", uniqueConstraints = @UniqueConstraint(columnNames = "name", name = "change_notices_unique_name_idx"))
@Getter
@Setter
@NoArgsConstructor
public class ChangeNotice extends NamedEntity implements HasId {

    @NotNull
    @Column(name = "release_date")
    private LocalDate releaseDate;

    @NotNull(groups = NotAutoGenerated.class)
    @Enumerated(EnumType.STRING)
    @Column(name = "change_reason_code")
    private ChangeReasonCode changeReasonCode;

    @Column(name = "auto_generated", nullable = false, columnDefinition = "bool default false")
    private boolean autoGenerated = false;

    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "developer_id")
    private Department developer;

    @NotNull(groups = NotAutoGenerated.class)
    @Embedded
    @Valid
    private ContentFile file;

    @Valid
    @NotEmpty
    @OneToMany(mappedBy = "changeNotice", fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE}, orphanRemoval = true)
    List<Change> changes;

    public static ChangeNotice autoGenerate(String name, LocalDate releaseDate) {
        ChangeNotice changeNotice = new ChangeNotice();
        changeNotice.setName(name);
        changeNotice.setReleaseDate(releaseDate);
        changeNotice.setAutoGenerated(true);
        return changeNotice;
    }

    public ChangeNotice(Long id, String name, LocalDate releaseDate, ChangeReasonCode changeReasonCode,
                        Department developer, ContentFile file) {
        super(id, name);
        this.releaseDate = releaseDate;
        this.changeReasonCode = changeReasonCode;
        this.developer = developer;
        this.file = file;
    }

    public void addChange(Change change) {
        if (changes == null) {
            changes = new ArrayList<>();
        }
        changes.add(change);
        change.setChangeNotice(this);
    }

    public void removeChange(Change change) {
        changes.remove(change);
        change.setChangeNotice(null);
    }
}
