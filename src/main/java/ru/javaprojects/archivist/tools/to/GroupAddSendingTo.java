package ru.javaprojects.archivist.tools.to;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;
import ru.javaprojects.archivist.common.to.BaseSendingToExtra;
import ru.javaprojects.archivist.tools.web.TxtFile;

@Getter
@Setter
public class GroupAddSendingTo extends BaseSendingToExtra {
    @NotNull
    @TxtFile
    private MultipartFile file;
}
