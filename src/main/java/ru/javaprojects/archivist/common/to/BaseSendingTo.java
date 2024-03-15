package ru.javaprojects.archivist.common.to;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.archivist.common.util.validation.NoHtml;
import ru.javaprojects.archivist.documents.model.Status;
import ru.javaprojects.archivist.documents.to.NotOriginalStatus;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BaseSendingTo {
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

    protected BaseSendingTo(Long companyId, Status status, String invoiceNumber, LocalDate invoiceDate,
                     String letterNumber, LocalDate letterDate) {
        this.companyId = companyId;
        this.status = status;
        this.invoiceNumber = invoiceNumber;
        this.invoiceDate = invoiceDate;
        this.letterNumber = letterNumber;
        this.letterDate = letterDate;
    }

    @Override
    public String toString() {
        return String.format("BaseSendingTo[companyId=%d, status=%s]", companyId, status);
    }
}
