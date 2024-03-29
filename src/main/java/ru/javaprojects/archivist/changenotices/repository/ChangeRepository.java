package ru.javaprojects.archivist.changenotices.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.archivist.changenotices.model.Change;
import ru.javaprojects.archivist.common.BaseRepository;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface ChangeRepository extends BaseRepository<Change> {

    @EntityGraph(attributePaths = "changeNotice")
    List<Change> findAllByDocument_IdOrderByChangeNumberDesc(long documentId);

    @Query("SELECT c FROM Change c JOIN FETCH c.document WHERE UPPER(c.changeNotice.name) =UPPER(:name) " +
            "AND c.changeNotice.autoGenerated = TRUE ORDER BY c.document.decimalNumber")
    List<Change> findAllForAutogeneratedChangeNotice(String name);

    @EntityGraph(attributePaths = "changeNotice")
    Optional<Change> findById(long id);

    long countAllByChangeNotice_Id(long changeNoticeId);
}
