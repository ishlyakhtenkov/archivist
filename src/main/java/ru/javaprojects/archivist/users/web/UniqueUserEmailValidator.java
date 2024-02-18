package ru.javaprojects.archivist.users.web;

import org.springframework.stereotype.Component;
import ru.javaprojects.archivist.common.util.validation.UniqueEmailValidator;
import ru.javaprojects.archivist.users.User;
import ru.javaprojects.archivist.users.UserRepository;

@Component
public class UniqueUserEmailValidator extends UniqueEmailValidator<User, UserRepository> {
    public UniqueUserEmailValidator(UserRepository repository) {
        super(repository);
    }
}
