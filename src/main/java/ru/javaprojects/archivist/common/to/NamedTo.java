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
public abstract class NamedTo extends BaseTo {

    @NotBlank
    @NoHtml
    @Size(min = 2, max = 128)
    protected String name;

    protected NamedTo(Long id, String name) {
        super(id);
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format(getClass().getSimpleName() + "[id=%d, name=%s]", id, name);
    }
}
