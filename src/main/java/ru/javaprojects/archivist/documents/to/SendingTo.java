package ru.javaprojects.archivist.documents.to;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.archivist.common.to.BaseTo;
import ru.javaprojects.archivist.common.util.validation.NoHtml;
import ru.javaprojects.archivist.documents.model.Status;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class SendingTo extends BaseTo {

    @NotNull
    private Long documentId;

    @NotNull
    private Long companyId;

    @NotNull
    @NotOriginalStatus
    private Status status;

    @NotBlank
    @NoHtml
    @Size(max = 10)
    private String invoiceNumber;

    @NotNull
    private LocalDate invoiceDate;

    @NoHtml
    @Size(max = 16)
    private String letterNumber;

    private LocalDate letterDate;
}
