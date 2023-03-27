package ru.javaprojects.archivist.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.archivist.model.User;

import java.util.Optional;

@Transactional(readOnly = true)
public interface UserRepository extends BaseRepository<User> {

    @Query("SELECT u FROM User u WHERE u.email = LOWER(:email)")
    Optional<User> findByEmailIgnoreCase(String email);

    Page<User> findAllByOrderByLastNameAscFirstName(Pageable pageable);

    Page<User> findAllByFirstNameContainsIgnoreCaseOrLastNameContainsIgnoreCaseOrEmailContainsIgnoreCaseOrderByLastNameAscFirstName(
            String firstNameKeyword, String lastNameKeyword, String emailKeyword, Pageable pageable);
}
