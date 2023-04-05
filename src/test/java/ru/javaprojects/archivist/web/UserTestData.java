package ru.javaprojects.archivist.web;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.javaprojects.archivist.MatcherFactory;
import ru.javaprojects.archivist.model.Role;
import ru.javaprojects.archivist.model.User;
import ru.javaprojects.archivist.to.UserTo;

import java.util.Set;
import java.util.stream.Collectors;

public class UserTestData {
    public static final MatcherFactory.Matcher<User> USER_MATCHER = MatcherFactory.usingIgnoringFieldsComparator(
            User.class, "password", "registered");

    public static final MatcherFactory.Matcher<UserTo> USER_TO_MATCHER = MatcherFactory.usingIgnoringFieldsComparator(UserTo.class);



    public static final long USER_ID = 100000;
    public static final long ADMIN_ID = 100001;
    public static final long NOT_FOUND = 100;
    public static final String USER_MAIL = "user@gmail.com";
    public static final String ADMIN_MAIL = "admin@gmail.com";

    public static final User user = new User(USER_ID, USER_MAIL, "John", "Doe", true, Set.of(Role.USER));
    public static final User admin = new User(ADMIN_ID, ADMIN_MAIL, "Jack", "London", true, Set.of(Role.USER, Role.ARCHIVIST, Role.ADMIN));

    public static User getNew() {
        return new User(null, "new@gmail.com", "newPassword", "newFirstName", "newLastName", true, Set.of(Role.USER, Role.ARCHIVIST));
    }

    public static User getUpdated() {
        return new User(USER_ID, "updated@gmail.com", user.getPassword(), "updatedFirstName", "updatedLastName", user.isEnabled(), Set.of(Role.USER, Role.ARCHIVIST, Role.ADMIN));
    }

    public static MultiValueMap<String, String> getNewParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        User newUser = getNew();
        params.add("firstName", newUser.getFirstName());
        params.add("lastName", newUser.getLastName());
        params.add("email", newUser.getEmail());
        params.add("roles", newUser.getRoles().stream().map(Enum::name).collect(Collectors.joining(",")));
        params.add("password", newUser.getPassword());
        params.add("enabled", newUser.isEnabled() + "");
        return params;
    }

    public static MultiValueMap<String, String> getNewInvalidParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("firstName", "");
        params.add("lastName","J");
        params.add("email", "notEmail");
        params.add("roles", "");
        params.add("password", "1111");
        params.add("enabled", Boolean.TRUE.toString());
        return params;
    }

    public static MultiValueMap<String, String> getUpdatedParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        User updatedUser = getUpdated();
        params.add("id", USER_ID + "");
        params.add("firstName", updatedUser.getFirstName());
        params.add("lastName", updatedUser.getLastName());
        params.add("email", updatedUser.getEmail());
        params.add("roles", updatedUser.getRoles().stream().map(Enum::name).collect(Collectors.joining(",")));
        return params;
    }

    public static MultiValueMap<String, String> getUpdatedInvalidParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("firstName", "");
        params.add("lastName","J");
        params.add("email", "notEmail");
        params.add("roles", "");
        return params;
    }
}
