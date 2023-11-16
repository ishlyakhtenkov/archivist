package ru.javaprojects.archivist.documents.to;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.archivist.common.to.BaseTo;
import ru.javaprojects.archivist.common.util.validation.NoHtml;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ChangeTo extends BaseTo {

    @NotNull
    private Long documentId;

    @NotBlank
    @NoHtml
    @Size(min = 2, max = 128)
    protected String changeNoticeName;

    @NotNull
    private LocalDate changeNoticeDate;

    @NotNull
    @PositiveOrZero
    private Integer changeNumber;
}
