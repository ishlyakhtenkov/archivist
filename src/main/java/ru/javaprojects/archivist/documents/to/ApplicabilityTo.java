package ru.javaprojects.archivist.documents.to;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.archivist.common.BaseTo;
import ru.javaprojects.archivist.common.util.validation.NoHtml;

@Getter
@Setter
@NoArgsConstructor
public class ApplicabilityTo extends BaseTo {

    @NotNull
    private Long documentId;

    @NotBlank
    @NoHtml
    @Size(max = 32)
    private String decimalNumber;

    private boolean primal = false;

    public ApplicabilityTo(Long id, Long documentId, String decimalNumber, boolean primal) {
        super(id);
        this.documentId = documentId;
        this.decimalNumber = decimalNumber;
        this.primal = primal;
    }
}
