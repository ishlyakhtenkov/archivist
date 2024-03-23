package ru.javaprojects.archivist.documents.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.archivist.common.BaseRepository;
import ru.javaprojects.archivist.documents.model.Content;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface ContentRepository extends BaseRepository<Content> {

    Optional<Content> findFirstByDocument_IdOrderByChangeNumberDesc(long documentId);

    List<Content> findByDocument_IdOrderByChangeNumberDesc(long documentId);

    @EntityGraph(attributePaths = "document")
    Optional<Content> findById(long id);

    @EntityGraph(attributePaths = "document")
    @Query("SELECT DISTINCT c1 FROM Content c1 JOIN Document d LEFT JOIN FETCH c1.files WHERE d.decimalNumber IN :decimalNumbers " +
            "AND c1.changeNumber = (SELECT MAX(c2.changeNumber) FROM Content c2 WHERE c2.document.id = c1.document.id)")
    List<Content> findLatestContentForDocuments(List<String> decimalNumbers);
}
