package ru.javaprojects.archivist.tools.to;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import ru.javaprojects.archivist.common.util.validation.NoHtml;

@Getter
@Setter
public class GroupUnsubscribeTo extends GroupSubscribeTo {
    @NotBlank
    @NoHtml
    @Size(max = 256)
    private String unsubscribeReason;
}
