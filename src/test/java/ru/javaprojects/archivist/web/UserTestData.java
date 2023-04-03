package ru.javaprojects.archivist.web;

import ru.javaprojects.archivist.MatcherFactory;
import ru.javaprojects.archivist.model.Role;
import ru.javaprojects.archivist.model.User;

import java.util.Set;

public class UserTestData {
    public static final MatcherFactory.Matcher<User> USER_MATCHER = MatcherFactory.usingIgnoringFieldsComparator(
            User.class, "password", "registered");


    public static final long USER_ID = 100000;
    public static final String USER_MAIL = "user@gmail.com";

    public static final User user = new User(USER_ID, USER_MAIL, "John", "Smith", true, Set.of(Role.USER));

}
