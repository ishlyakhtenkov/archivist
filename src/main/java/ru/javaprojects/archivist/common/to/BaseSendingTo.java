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
public abstract class BaseSendingTo {

    @NotNull
    @NotOriginalStatus
    protected Status status;

    @NotBlank
    @NoHtml
    @Size(max = 10)
    protected String invoiceNumber;

    @NotNull
    protected LocalDate invoiceDate;


    protected BaseSendingTo(Status status, String invoiceNumber, LocalDate invoiceDate) {
        this.status = status;
        this.invoiceNumber = invoiceNumber;
        this.invoiceDate = invoiceDate;
    }

    @Override
    public String toString() {
        return String.format("%s[status=%s, invoiceNumber=%s, invoiceDate=%s]", getClass().getSimpleName(), status,
                invoiceNumber, invoiceDate);
    }
}
