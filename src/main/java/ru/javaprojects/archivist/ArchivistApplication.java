package ru.javaprojects.archivist;

import lombok.Getter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import ru.javaprojects.archivist.users.User;

import static java.util.Objects.requireNonNull;

@SpringBootApplication
public class ArchivistApplication {

	public static void main(String[] args) {
		SpringApplication.run(ArchivistApplication.class, args);
	}

    public static class AuthUser extends org.springframework.security.core.userdetails.User {

        @Getter
        @NonNull
        private final User user;

        public AuthUser(@NonNull User user) {
            super(user.getEmail(), user.getPassword(), user.isEnabled(), true, true, true, user.getRoles());
            this.user = user;
        }

        public static AuthUser safeGet() {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) {
                return null;
            }
            Object principal = auth.getPrincipal();
            return (principal instanceof AuthUser au) ? au : null;
        }

        public static AuthUser get() {
            return requireNonNull(safeGet(), "No authorized user found");
        }

        public static User authUser() {
            return get().getUser();
        }

        public static long authId() {
            return get().id();
        }

        public long id() {
            return user.id();
        }

        @Override
        public String toString() {
            return "AuthUser:" + user.getId() + '[' + user.getEmail() + ']';
        }
    }
}
