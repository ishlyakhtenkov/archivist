package ru.javaprojects.archivist.tools.to;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import ru.javaprojects.archivist.tools.web.TxtFile;

@Getter
@Setter
public class GroupSubscribeTo {
    @NotNull
    private Long companyId;

    @NotNull
    @TxtFile
    private MultipartFile file;

    @Override
    public String toString() {
        return String.format("%s[companyId=%d]", getClass().getSimpleName(), companyId);
    }
}
