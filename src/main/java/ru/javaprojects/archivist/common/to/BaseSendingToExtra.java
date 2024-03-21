package ru.javaprojects.archivist.common.to;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.archivist.common.util.validation.NoHtml;
import ru.javaprojects.archivist.documents.model.Status;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class BaseSendingToExtra extends BaseSendingTo {
    @NotNull
    protected Long companyId;

    @NoHtml
    @Size(max = 16)
    protected String letterNumber;

    protected LocalDate letterDate;

    public BaseSendingToExtra(Long companyId, Status status, String invoiceNumber, LocalDate invoiceDate,
                              String letterNumber, LocalDate letterDate) {
        super(status, invoiceNumber, invoiceDate);
        this.companyId = companyId;
        this.letterNumber = letterNumber;
        this.letterDate = letterDate;
    }

    @Override
    public String toString() {
        return String.format("%s[companyId=%d, status=%s]", getClass().getSimpleName(), companyId, getStatus());
    }
}
