package ru.javaprojects.archivist.albums.to;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.archivist.albums.model.Stamp;
import ru.javaprojects.archivist.common.BaseTo;
import ru.javaprojects.archivist.common.util.validation.NoHtml;

@Getter
@Setter
@NoArgsConstructor
public class AlbumTo extends BaseTo {

    @NotBlank
    @NoHtml
    @Size(max = 32)
    private String decimalNumber;

    @NotNull
    private Stamp stamp;

    public AlbumTo(Long id, String decimalNumber, Stamp stamp) {
        super(id);
        this.decimalNumber = decimalNumber;
        this.stamp = stamp;
    }
}
