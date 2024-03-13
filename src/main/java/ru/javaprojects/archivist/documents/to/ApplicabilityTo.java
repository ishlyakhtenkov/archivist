package ru.javaprojects.archivist.documents.to;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.archivist.common.to.DecimalNumberTo;

@Getter
@Setter
@NoArgsConstructor
public class ApplicabilityTo extends DecimalNumberTo {

    @NotNull
    private Long documentId;

    private boolean primal = false;

    public ApplicabilityTo(Long id, Long documentId, String decimalNumber, boolean primal) {
        super(id, decimalNumber);
        this.documentId = documentId;
        this.primal = primal;
    }
}
