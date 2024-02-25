package ru.javaprojects.archivist.albums;

import lombok.experimental.UtilityClass;
import ru.javaprojects.archivist.albums.model.Album;
import ru.javaprojects.archivist.albums.to.AlbumTo;

@UtilityClass
public class AlbumUtil {
    public static AlbumTo asTo(Album album) {
        return new AlbumTo(album.getId(), album.getMainDocument().getDecimalNumber(), album.getStamp());
    }
}
