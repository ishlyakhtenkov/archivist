package ru.javaprojects.archivist.documents.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.archivist.common.BaseRepository;
import ru.javaprojects.archivist.documents.model.Sending;

import java.util.List;

@Transactional(readOnly = true)
public interface SendingRepository extends BaseRepository<Sending> {

    @Query("SELECT s FROM Sending s JOIN FETCH s.invoice JOIN FETCH s.invoice.letter " +
            "WHERE s.document.id =:documentId AND s.invoice.letter.company.id =:companyId ORDER BY s.invoice.date DESC")
    List<Sending> findAllByDocumentIdAndCompanyId(long documentId, long companyId);
}
