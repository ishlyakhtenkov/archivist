package ru.javaprojects.archivist.service;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.javaprojects.archivist.model.User;
import ru.javaprojects.archivist.repository.UserRepository;
import ru.javaprojects.archivist.to.UserTo;
import ru.javaprojects.archivist.util.exception.NotFoundException;

import static ru.javaprojects.archivist.config.SecurityConfig.PASSWORD_ENCODER;
import static ru.javaprojects.archivist.util.UserUtil.prepareToSave;
import static ru.javaprojects.archivist.util.UserUtil.updateFromTo;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository repository;

    public Page<User> getAll(Pageable pageable) {
        return repository.findAllByOrderByLastNameAscFirstName(pageable);
    }

    public Page<User> getAllByKeyword(Pageable pageable, String keyword) {
        return repository.findAllByFirstNameContainsIgnoreCaseOrLastNameContainsIgnoreCaseOrEmailContainsIgnoreCaseOrderByLastNameAscFirstName(
                keyword, keyword, keyword, pageable);
    }

    public User get(long id) {
        return repository.getExisted(id);
    }

    public User getByEmail(String email) {
        return repository.findByEmailIgnoreCase(email).orElseThrow(() ->
                new NotFoundException("Not found user with email=" + email));
    }

    public void create(User user) {
        Assert.notNull(user, "user must not be null");
        repository.save(prepareToSave(user));
    }

    @Transactional
    public void update(UserTo userTo) {
        Assert.notNull(userTo, "userTo must not be null");
        User user = get(userTo.getId());
        updateFromTo(user, userTo);
    }

    @Transactional
    public void enable(long id, boolean enabled) {
        User user = repository.getExisted(id);
        user.setEnabled(enabled);
    }

    public void delete(long id) {
        repository.deleteExisted(id);
    }

    @Transactional
    public void changePassword(long id, String password) {
        Assert.notNull(password, "password must not be null");
        User user = get(id);
        user.setPassword(PASSWORD_ENCODER.encode(password));
    }
}
