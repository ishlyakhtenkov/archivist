package ru.javaprojects.archivist.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.javaprojects.archivist.model.User;
import ru.javaprojects.archivist.repository.UserRepository;

import static ru.javaprojects.archivist.util.UserUtil.prepareToSave;

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

    public void create(User user) {
        Assert.notNull(user, "user must not be null");
        repository.save(prepareToSave(user));
    }

    @Transactional
    public void enable(long id, boolean enabled) {
        User user = repository.getExisted(id);
        user.setEnabled(enabled);
    }

    public void delete(long id) {
        repository.deleteExisted(id);
    }
}
