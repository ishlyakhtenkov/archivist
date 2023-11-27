package ru.javaprojects.archivist.documents.repository;

import org.springframework.data.jpa.repository.EntityGraph;
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
}
