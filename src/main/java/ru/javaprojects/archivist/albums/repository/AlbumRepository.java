package ru.javaprojects.archivist.albums.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.archivist.albums.model.Album;
import ru.javaprojects.archivist.albums.model.Stamp;
import ru.javaprojects.archivist.common.BaseRepository;

import java.util.Optional;

@Transactional(readOnly = true)
public interface AlbumRepository extends BaseRepository<Album> {

    // use count query to avoid Hibernate exception when fetch
    // https://stackoverflow.com/questions/12459779/query-specified-join-fetching-but-the-owner-of-the-fetched-association-was-not
    @Query(value = "SELECT a FROM Album a JOIN FETCH a.mainDocument md ORDER BY md.decimalNumber",
            countQuery = "SELECT count(a) FROM Album a")
    Page<Album> findAllWithPagination(Pageable pageable);

    @Query(value = "SELECT a FROM Album a JOIN FETCH a.mainDocument md WHERE UPPER(md.decimalNumber) LIKE " +
            "UPPER(CONCAT('%', :keyword, '%')) ORDER BY md.decimalNumber", countQuery = "SELECT count(a) FROM Album a")
    Page<Album> findAllByKeywordWithPagination(Pageable pageable, String keyword);

    @EntityGraph(attributePaths = "mainDocument")
    Optional<Album> findById(long id);

    Optional<Album> findByMainDocument_DecimalNumberIgnoreCaseAndStamp(String decimalNumber, Stamp stamp);
}
