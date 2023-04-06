package ru.javaprojects.archivist.users;

import lombok.experimental.UtilityClass;
import org.springframework.util.StringUtils;

import static ru.javaprojects.archivist.common.config.SecurityConfig.PASSWORD_ENCODER;

@UtilityClass
public class UserUtil {

    public static User prepareToSave(User user) {
        String password = user.getPassword();
        user.setPassword(StringUtils.hasText(password) ? PASSWORD_ENCODER.encode(password) : password);
        user.setEmail(user.getEmail().toLowerCase());
        return user;
    }

    public static UserTo asTo(User user) {
        return new UserTo(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getRoles());
    }

    public static User updateFromTo(User user, UserTo userTo) {
        user.setEmail(userTo.getEmail());
        user.setFirstName(userTo.getFirstName());
        user.setLastName(userTo.getLastName());
        user.setRoles(userTo.getRoles());
        return user;
    }
}
