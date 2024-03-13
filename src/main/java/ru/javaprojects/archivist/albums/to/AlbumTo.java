package ru.javaprojects.archivist.albums.to;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.archivist.albums.model.Stamp;
import ru.javaprojects.archivist.common.to.DecimalNumberTo;

@Getter
@Setter
@NoArgsConstructor
public class AlbumTo extends DecimalNumberTo {

    @NotNull
    private Stamp stamp;

    public AlbumTo(Long id, String decimalNumber, Stamp stamp) {
        super(id, decimalNumber);
        this.stamp = stamp;
    }
}
