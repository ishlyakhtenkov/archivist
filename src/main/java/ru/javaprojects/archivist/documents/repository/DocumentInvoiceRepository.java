package ru.javaprojects.archivist.documents.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.archivist.common.BaseRepository;
import ru.javaprojects.archivist.documents.model.DocumentInvoice;

import java.util.List;

@Transactional(readOnly = true)
public interface DocumentInvoiceRepository extends BaseRepository<DocumentInvoice> {

    @Query("SELECT di FROM DocumentInvoice di JOIN FETCH di.invoice JOIN FETCH di.invoice.letter " +
            "WHERE di.document.id =:documentId AND di.invoice.letter.company.id =:companyId ORDER BY di.invoice.date DESC")
    List<DocumentInvoice> findAllByDocumentIdAndCompanyId(long documentId, long companyId);
}
