package ru.javaprojects.archivist.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.javaprojects.archivist.model.User;
import ru.javaprojects.archivist.repository.UserRepository;

@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository repository;

    @Transactional
    public void enable(long id, boolean enabled) {
        User user = repository.getExisted(id);
        user.setEnabled(enabled);
    }
}
