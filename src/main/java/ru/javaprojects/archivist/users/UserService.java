package ru.javaprojects.archivist.users;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.javaprojects.archivist.common.error.NotFoundException;

import static ru.javaprojects.archivist.common.config.SecurityConfig.PASSWORD_ENCODER;
import static ru.javaprojects.archivist.users.UserUtil.prepareToSave;
import static ru.javaprojects.archivist.users.UserUtil.updateFromTo;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository repository;

    public Page<User> getAll(Pageable pageable) {
        return repository.findAllWithPagination(pageable);
    }

    public Page<User> getAll(Pageable pageable, String keyword) {
        return repository.findAllByKeywordWithPagination(keyword, pageable);
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
        User user = get(id);
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
