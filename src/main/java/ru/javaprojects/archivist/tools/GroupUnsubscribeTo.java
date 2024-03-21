package ru.javaprojects.archivist.tools;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import ru.javaprojects.archivist.common.util.validation.NoHtml;
import ru.javaprojects.archivist.tools.web.TxtFile;

@Getter
@Setter
public class GroupUnsubscribeTo {
    @NotNull
    private Long companyId;

    @NotBlank
    @NoHtml
    @Size(max = 256)
    private String unsubscribeReason;

    @NotNull
    @TxtFile
    private MultipartFile file;

    @Override
    public String toString() {
        return String.format("GroupUnsubscribeTo[companyId=%d]", getCompanyId());
    }
}
