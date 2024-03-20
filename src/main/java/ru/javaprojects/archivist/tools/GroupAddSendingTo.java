package ru.javaprojects.archivist.tools;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import ru.javaprojects.archivist.common.to.BaseSendingTo;
import ru.javaprojects.archivist.documents.model.Status;
import ru.javaprojects.archivist.tools.web.TxtFile;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class GroupAddSendingTo extends BaseSendingTo {
    @NotNull
    @TxtFile
    private MultipartFile file;

    public GroupAddSendingTo(Long companyId, Status status, String invoiceNumber, LocalDate invoiceDate, String letterNumber,
                             LocalDate letterDate, MultipartFile file) {
        super(companyId, status, invoiceNumber, invoiceDate, letterNumber, letterDate);
        this.file = file;
    }

    @Override
    public String toString() {
        return String.format("GroupSendingTo[companyId=%d, status=%s]", getCompanyId(), getStatus());
    }
}
