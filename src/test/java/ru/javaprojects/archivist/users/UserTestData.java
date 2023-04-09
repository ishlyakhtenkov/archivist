package ru.javaprojects.archivist.users;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.javaprojects.archivist.MatcherFactory;
import ru.javaprojects.archivist.users.Role;
import ru.javaprojects.archivist.users.User;
import ru.javaprojects.archivist.users.UserTo;
import ru.javaprojects.archivist.users.password_reset.PasswordResetToken;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

public class UserTestData {
    public static final MatcherFactory.Matcher<User> USER_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(User.class, "password", "registered");

    public static final MatcherFactory.Matcher<UserTo> USER_TO_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(UserTo.class);

    public static final long USER_ID = 100000;
    public static final long ADMIN_ID = 100001;
    public static final long NOT_FOUND = 100;
    public static final String USER_MAIL = "user@gmail.com";
    public static final String ADMIN_MAIL = "admin@gmail.com";

    public static final String NEW_PASSWORD = "newPassword";
    public static final String INVALID_PASSWORD = "pass";
    public static final String CHANGE_PASSWORD_LENGTH_ERROR = "changePassword.password: size must be between 5 and 32";
    public static final String PASSWORD = "password";
    public static final String ENABLED = "enabled";
    public static final String FALSE = "false";
    public static final String TRUE = "true";
    public static final String ENTITY_NOT_FOUND = "Entity with id=" + NOT_FOUND + " not found";
    public static final String USER_ATTRIBUTE = "user";
    public static final String USER_TO_ATTRIBUTE = "userTo";
    public static final String USERS_PAGE_ATTRIBUTE = "usersPage";
    public static final String ID = "id";
    public static final String FIRST_NAME = "firstName";
    public static final String LAST_NAME = "lastName";
    public static final String EMAIL = "email";
    public static final String ROLES = "roles";

    public static final User user = new User(USER_ID, USER_MAIL, "John", "Doe", true, Set.of(Role.USER));
    public static final User admin = new User(ADMIN_ID, ADMIN_MAIL, "Jack", "London", true, Set.of(Role.USER, Role.ARCHIVIST, Role.ADMIN));

    public static final PasswordResetToken adminPasswordResetToken = new PasswordResetToken(100004L,
            "5a99dd09-d23f-44bb-8d41-b6ff44275d01",parseDate("2052-02-05 12:10:00"));

    public static User getNew() {
        return new User(null, "new@gmail.com", "newPassword", "newFirstName", "newLastName", true, Set.of(Role.USER, Role.ARCHIVIST));
    }

    public static User getUpdated() {
        return new User(USER_ID, "updated@gmail.com", user.getPassword(), "updatedFirstName", "updatedLastName", user.isEnabled(), Set.of(Role.USER, Role.ARCHIVIST, Role.ADMIN));
    }

    public static MultiValueMap<String, String> getPageableParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("page", "0");
        params.add("size", "2");
        return params;
    }

    public static MultiValueMap<String, String> getNewParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        User newUser = getNew();
        params.add(FIRST_NAME, newUser.getFirstName());
        params.add(LAST_NAME, newUser.getLastName());
        params.add(EMAIL, newUser.getEmail());
        params.add(ROLES, newUser.getRoles().stream().map(Enum::name).collect(Collectors.joining(",")));
        params.add(PASSWORD, newUser.getPassword());
        params.add(ENABLED, newUser.isEnabled() + "");
        return params;
    }

    public static MultiValueMap<String, String> getNewInvalidParams() {
        MultiValueMap<String, String> params = getUpdatedInvalidParams();
        params.add(PASSWORD, INVALID_PASSWORD);
        params.add(ENABLED, TRUE);
        return params;
    }

    public static MultiValueMap<String, String> getUpdatedParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        User updatedUser = getUpdated();
        params.add(ID, USER_ID + "");
        params.add(FIRST_NAME, updatedUser.getFirstName());
        params.add(LAST_NAME, updatedUser.getLastName());
        params.add(EMAIL, updatedUser.getEmail());
        params.add(ROLES, updatedUser.getRoles().stream().map(Enum::name).collect(Collectors.joining(",")));
        return params;
    }

    public static MultiValueMap<String, String> getUpdatedInvalidParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(FIRST_NAME, "");
        params.add(LAST_NAME,"J");
        params.add(EMAIL, "notEmail");
        params.add(ROLES, "");
        return params;
    }

    private static Date parseDate(String date) {
        try {
            return new SimpleDateFormat("yyyy-MM-dd Hh:mm:ss").parse(date);
        } catch (ParseException e) {
            return null;
        }
    }
}
