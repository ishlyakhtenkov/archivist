package ru.javaprojects.archivist.albums.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.archivist.albums.model.Issuance;
import ru.javaprojects.archivist.common.BaseRepository;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface IssuanceRepository extends BaseRepository<Issuance> {

    @Query("SELECT i FROM Issuance i JOIN FETCH i.employee e JOIN FETCH e.department " +
            "WHERE i.album.id =:albumId ORDER BY i.issued DESC, i.returned")
    List<Issuance> findAllByAlbumId(long albumId);

    Optional<Issuance> findByAlbum_IdAndReturnedIsNull(long id);

    Optional<Issuance> findFirstByAlbum_IdOrderByIssuedDesc(long id);
}
