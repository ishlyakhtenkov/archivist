package ru.javaprojects.archivist.changenotices.to;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.archivist.common.to.DecimalNumberTo;

@Getter
@Setter
@NoArgsConstructor
public class ChangeTo extends DecimalNumberTo {

    @NotNull
    @PositiveOrZero
    private Integer changeNumber;

    public ChangeTo(Long id, String decimalNumber, Integer changeNumber) {
        super(id, decimalNumber);
        this.changeNumber = changeNumber;
    }
}
