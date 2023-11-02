package ru.javaprojects.archivist.documents.repository;

import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.archivist.common.BaseRepository;
import ru.javaprojects.archivist.documents.model.DocumentInvoice;

@Transactional(readOnly = true)
public interface DocumentInvoiceRepository extends BaseRepository<DocumentInvoice> {

}
