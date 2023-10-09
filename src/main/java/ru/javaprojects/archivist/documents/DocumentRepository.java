package ru.javaprojects.archivist.documents;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.archivist.common.NamedRepository;
import ru.javaprojects.archivist.documents.model.Document;

import java.util.Optional;

@Transactional(readOnly = true)
public interface DocumentRepository extends NamedRepository<Document> {

    @EntityGraph(attributePaths = "originalHolder")
    Page<Document> findAllByOrderByDecimalNumber(Pageable pageable);

    @EntityGraph(attributePaths = "originalHolder")
    Page<Document> findAllByDecimalNumberContainsIgnoreCaseOrderByDecimalNumber(Pageable pageable, String keyword);

    @EntityGraph(attributePaths = {"originalHolder", "developer"})
    Optional<Document> findById(long id);
}
