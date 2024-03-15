package ru.javaprojects.archivist.documents.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.archivist.common.NamedRepository;
import ru.javaprojects.archivist.documents.model.Document;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface DocumentRepository extends NamedRepository<Document> {

    @EntityGraph(attributePaths = "originalHolder")
    Page<Document> findAllByAutoGeneratedIsFalseOrderByDecimalNumber(Pageable pageable);

    @EntityGraph(attributePaths = "originalHolder")
    Page<Document> findAllByDecimalNumberContainsIgnoreCaseAndAutoGeneratedIsFalseOrderByDecimalNumber(Pageable pageable,
                                                                                                       String keyword);

    @EntityGraph(attributePaths = {"originalHolder", "developer"})
    Optional<Document> findByIdAndAutoGeneratedIsFalse(long id);

    Optional<Document> findByDecimalNumberIgnoreCase(String decimalNumber);

    Optional<Document> findByDecimalNumberAndAutoGeneratedIsTrueIgnoreCase(String decimalNumber);

    Optional<Document> findByInventoryNumberIgnoreCase(String inventoryNumber);

    List<Document> findAllByAutoGeneratedIsFalseAndDecimalNumberIn(List<String> decimalNumbers);
}
