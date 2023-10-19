package ru.javaprojects.archivist.documents.to;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import ru.javaprojects.archivist.common.to.BaseTo;
import ru.javaprojects.archivist.common.util.validation.NoHtml;

@Getter
public class ApplicabilityTo extends BaseTo {

    @NotNull
    private Long documentId;

    @NotBlank
    @NoHtml
    @Size(max = 32)
    private String decimalNumber;

    private boolean primal = false;
}
