package ru.javaprojects.archivist.documents.repository;

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
}
