package ru.javaprojects.archivist.changenotices.to;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.archivist.common.to.BaseTo;
import ru.javaprojects.archivist.common.util.validation.NoHtml;

@Getter
@Setter
@NoArgsConstructor
public class ChangeTo extends BaseTo {

    @NotBlank
    @NoHtml
    @Size(max = 32)
    private String decimalNumber;

    @NotNull
    @PositiveOrZero
    private Integer changeNumber;

    public ChangeTo(Long id, String decimalNumber, Integer changeNumber) {
        super(id);
        this.decimalNumber = decimalNumber;
        this.changeNumber = changeNumber;
    }
}
