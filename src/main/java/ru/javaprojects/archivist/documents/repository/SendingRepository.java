package ru.javaprojects.archivist.documents.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.archivist.common.BaseRepository;
import ru.javaprojects.archivist.documents.model.Sending;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Transactional(readOnly = true)
public interface SendingRepository extends BaseRepository<Sending> {

    @Query("SELECT s FROM Sending s JOIN FETCH s.invoice JOIN FETCH s.invoice.letter " +
            "WHERE s.document.id =:documentId AND s.invoice.letter.company.id =:companyId ORDER BY s.invoice.date DESC")
    List<Sending> findAllByDocumentIdAndCompanyId(long documentId, long companyId);

    @Query("SELECT s FROM Sending s JOIN FETCH s.invoice JOIN FETCH s.invoice.letter JOIN FETCH s.invoice.letter.company " +
            "WHERE s.id =:id")
    Optional<Sending> findByIdWithInvoice(long id);

    long countAllByInvoice_Id(long invoiceId);

    @Query("SELECT s.document.id FROM Sending s WHERE s.invoice.id =:invoiceId")
    Set<Long> findDocumentsIdsByInvoice(long invoiceId);
}
