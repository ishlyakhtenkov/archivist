package ru.javaprojects.archivist.documents.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.archivist.common.BaseRepository;
import ru.javaprojects.archivist.documents.model.Applicability;

import java.util.List;

@Transactional(readOnly = true)
public interface ApplicabilityRepository extends BaseRepository<Applicability> {

    @Query("SELECT a FROM Applicability a JOIN FETCH a.applicability WHERE a.document.id =:documentId ORDER BY a.applicability.decimalNumber")
    List<Applicability> findAllByDocumentId(long documentId);
}
