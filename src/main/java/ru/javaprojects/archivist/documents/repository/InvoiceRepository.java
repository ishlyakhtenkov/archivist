package ru.javaprojects.archivist.documents.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.archivist.common.BaseRepository;
import ru.javaprojects.archivist.documents.model.Invoice;

import java.time.LocalDate;
import java.util.Optional;

@Transactional(readOnly = true)
public interface InvoiceRepository extends BaseRepository<Invoice> {

    @EntityGraph(attributePaths = "letter.company")
    Optional<Invoice> findByNumberAndDate(String number, LocalDate date);

    @EntityGraph(attributePaths = {"letter.company", "sendings.document"})
    Optional<Invoice> findWithSendingsByNumberAndDate(String number, LocalDate date);

    long countAllByLetter_Id(long letterId);
}
