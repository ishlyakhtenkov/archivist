package ru.javaprojects.archivist.albums.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.archivist.albums.model.Album;
import ru.javaprojects.archivist.albums.model.Stamp;
import ru.javaprojects.archivist.common.BaseRepository;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface AlbumRepository extends BaseRepository<Album> {

    // use two below queries to get Albums page, because of pagination and JOIN FETCH
    // https://stackoverflow.com/questions/64799564/spring-data-jpa-pagination-hhh000104
    @Query(value = "SELECT a.id FROM Album a JOIN a.mainDocument md ORDER BY md.decimalNumber",
            countQuery = "SELECT count(a) FROM Album a")
    Page<Long> findAllAlbumsIdsWithPagination(Pageable pageable);

    @Query("SELECT a FROM Album a JOIN FETCH a.mainDocument md LEFT JOIN FETCH a.issuances i " +
            "LEFT JOIN FETCH i.employee e LEFT JOIN FETCH e.department WHERE a.id IN :ids ORDER BY md.decimalNumber")
    List<Album> findAllByIds(List<Long> ids);

    @Query(value = "SELECT a.id FROM Album a JOIN a.mainDocument md WHERE UPPER(md.decimalNumber) LIKE " +
            "UPPER(CONCAT('%', :keyword, '%')) ORDER BY md.decimalNumber",
            countQuery = "SELECT count(a) FROM Album a JOIN a.mainDocument md WHERE UPPER(md.decimalNumber) LIKE " +
                    "UPPER(CONCAT('%', :keyword, '%'))")
    Page<Long> findAllAlbumsIdsByKeywordWithPagination(Pageable pageable, String keyword);

    @EntityGraph(attributePaths = "mainDocument")
    Optional<Album> findById(long id);

    Optional<Album> findByMainDocument_DecimalNumberIgnoreCaseAndStamp(String decimalNumber, Stamp stamp);

    @Query("SELECT a FROM Album a JOIN FETCH a.mainDocument LEFT JOIN FETCH a.issuances i LEFT JOIN FETCH i.employee e " +
            "LEFT JOIN FETCH e.department WHERE a.id =:id ORDER BY i.issued DESC, i.returned NULLS FIRST")
    Optional<Album> findByIdWithIssuances(long id);

    @EntityGraph(attributePaths = "mainDocument")
    Optional<Album> findByMainDocument_DecimalNumberAndStamp(String decimalNumber, Stamp stamp);
}
