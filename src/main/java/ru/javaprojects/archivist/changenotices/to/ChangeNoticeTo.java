package ru.javaprojects.archivist.changenotices.to;

import jakarta.annotation.Nullable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import ru.javaprojects.archivist.changenotices.model.ChangeReasonCode;
import ru.javaprojects.archivist.changenotices.web.PdfFile;
import ru.javaprojects.archivist.common.to.NamedTo;
import ru.javaprojects.archivist.departments.model.Department;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ChangeNoticeTo extends NamedTo {

    @NotNull
    private LocalDate releaseDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ChangeReasonCode changeReasonCode;

    @Nullable
    private Department developer;

    @Nullable
    @PdfFile
    private MultipartFile file;

    @Valid
    @NotEmpty
    private List<ChangeTo> changes;

    public ChangeNoticeTo(Long id, String name, LocalDate releaseDate, ChangeReasonCode changeReasonCode,
                          Department developer, List<ChangeTo> changes) {
        super(id, name);
        this.releaseDate = releaseDate;
        this.changeReasonCode = changeReasonCode;
        this.developer = developer;
        this.changes = changes;
    }

    public ChangeNoticeTo(Long id, String name, LocalDate releaseDate, ChangeReasonCode changeReasonCode,
                          Department developer, MultipartFile file, List<ChangeTo> changes) {
        this(id, name, releaseDate, changeReasonCode, developer, changes);
        this.file = file;
    }
}
