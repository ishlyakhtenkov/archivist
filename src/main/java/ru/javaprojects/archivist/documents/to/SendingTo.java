package ru.javaprojects.archivist.documents.to;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.archivist.common.to.BaseSendingToExtra;
import ru.javaprojects.archivist.documents.model.Status;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class SendingTo extends BaseSendingToExtra {

    @NotNull
    private Long documentId;

    public SendingTo(Long documentId, Long companyId, Status status, String invoiceNumber, LocalDate invoiceDate,
                     String letterNumber, LocalDate letterDate) {
        super(companyId, status, invoiceNumber, invoiceDate, letterNumber, letterDate);
        this.documentId = documentId;
    }

    @Override
    public String toString() {
        return String.format("SendingTo[documentId=%d, companyId=%d, status=%s]", documentId, getCompanyId(), getStatus());
    }
}
