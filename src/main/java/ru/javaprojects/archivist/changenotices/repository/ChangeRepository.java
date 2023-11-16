package ru.javaprojects.archivist.changenotices.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.archivist.changenotices.model.Change;
import ru.javaprojects.archivist.common.BaseRepository;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface ChangeRepository extends BaseRepository<Change> {

    @EntityGraph(attributePaths = "changeNotice")
    List<Change> findAllByDocument_IdOrderByChangeNumberDesc(long documentId);

    @EntityGraph(attributePaths = "changeNotice")
    Optional<Change> findById(long id);

    long countAllByChangeNotice_Id(long changeNoticeId);
}
