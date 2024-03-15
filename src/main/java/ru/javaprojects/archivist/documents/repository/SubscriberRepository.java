package ru.javaprojects.archivist.documents.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.archivist.common.BaseRepository;
import ru.javaprojects.archivist.documents.model.Subscriber;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface SubscriberRepository extends BaseRepository<Subscriber> {

    @Query("SELECT s FROM Subscriber s JOIN FETCH s.company WHERE s.document.id =:documentId ORDER BY s.company.name")
    List<Subscriber> findAllByDocumentId(long documentId);

    Optional<Subscriber> findByDocument_IdAndCompany_Id(long documentId, long companyId);

    void deleteByDocument_IdAndCompany_Id(long documentId, long companyId);

    @EntityGraph(attributePaths = "document")
    List<Subscriber> findAllByCompany_IdAndDocument_IdIn(long companyId, List<Long> documentsIds);
}
