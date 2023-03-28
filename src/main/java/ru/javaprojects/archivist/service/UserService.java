package ru.javaprojects.archivist.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.archivist.model.User;
import ru.javaprojects.archivist.repository.UserRepository;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository repository;

    public Page<User> findAll(Pageable pageable) {
        return repository.findAllByOrderByLastNameAscFirstName(pageable);
    }

    public Page<User> findAllByKeyword(Pageable pageable, String keyword) {
        return repository.findAllByFirstNameContainsIgnoreCaseOrLastNameContainsIgnoreCaseOrEmailContainsIgnoreCaseOrderByLastNameAscFirstName(
                keyword, keyword, keyword, pageable);
    }

    @Transactional
    public void enable(long id, boolean enabled) {
        User user = repository.getExisted(id);
        user.setEnabled(enabled);
    }
}
