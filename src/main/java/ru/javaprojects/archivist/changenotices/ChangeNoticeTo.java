package ru.javaprojects.archivist.changenotices;

import jakarta.annotation.Nullable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import ru.javaprojects.archivist.changenotices.model.Change;
import ru.javaprojects.archivist.changenotices.model.ChangeReasonCode;
import ru.javaprojects.archivist.common.to.BaseTo;
import ru.javaprojects.archivist.common.util.validation.NoHtml;
import ru.javaprojects.archivist.departments.Department;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ChangeNoticeTo extends BaseTo {

    @NotBlank
    @NoHtml
    @Size(min = 2, max = 128)
    protected String name;

    @NotNull
    private LocalDate releaseDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ChangeReasonCode changeReasonCode;

    @Nullable
    private Department developer;

    @Nullable
    //TODO pdf validator
    private MultipartFile file;

    @Valid
    @NotEmpty
    private List<Change> changes;
}
