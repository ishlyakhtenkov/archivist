package ru.javaprojects.archivist.users;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.archivist.common.EmailedRepository;

@Transactional(readOnly = true)
public interface UserRepository extends EmailedRepository<User> {

    @Query("SELECT u FROM User u ORDER BY u.lastName, u.firstName")
    Page<User> findAllWithPagination(Pageable pageable);

    @Query("SELECT u FROM User u WHERE UPPER(u.firstName) LIKE UPPER(CONCAT('%', :keyword, '%')) OR " +
            "UPPER(u.lastName) LIKE UPPER(CONCAT('%', :keyword, '%')) OR " +
            "UPPER(u.email) LIKE UPPER(CONCAT('%', :keyword, '%')) ORDER BY u.lastName, u.firstName")
    Page<User> findAllByKeywordWithPagination(String keyword, Pageable pageable);
}
