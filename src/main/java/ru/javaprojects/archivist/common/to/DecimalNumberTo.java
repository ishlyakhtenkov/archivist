package ru.javaprojects.archivist.common.to;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.archivist.common.util.validation.NoHtml;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class DecimalNumberTo extends BaseTo {

    @NotBlank
    @NoHtml
    @Size(max = 32)
    protected String decimalNumber;

    protected DecimalNumberTo(Long id, String decimalNumber) {
        super(id);
        this.decimalNumber = decimalNumber;
    }

    @Override
    public String toString() {
        return String.format(getClass().getSimpleName() + "[id=%d, decimalNumber=%s]", id, decimalNumber);
    }
}
