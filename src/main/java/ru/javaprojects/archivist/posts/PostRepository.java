package ru.javaprojects.archivist.posts;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.archivist.common.BaseRepository;

@Transactional(readOnly = true)
public interface PostRepository extends BaseRepository<Post> {

    @EntityGraph(attributePaths = "author")
    Page<Post> findAllByOrderByCreatedDesc(Pageable pageable);

    @EntityGraph(attributePaths = "author")
    Page<Post> findAllByForAuthOnlyIsFalseOrderByCreatedDesc(Pageable pageable);
}
