package ru.javaprojects.archivist.tools;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import ru.javaprojects.archivist.common.util.validation.NoHtml;
import ru.javaprojects.archivist.documents.model.Status;
import ru.javaprojects.archivist.documents.to.NotOriginalStatus;
import ru.javaprojects.archivist.tools.web.TxtFile;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class GroupDeleteSendingTo {
    @NotNull
    @NotOriginalStatus
    private Status status;

    @NotBlank
    @NoHtml
    @Size(max = 10)
    private String invoiceNumber;

    @NotNull
    private LocalDate invoiceDate;

    @NotNull
    @TxtFile
    private MultipartFile file;
}
