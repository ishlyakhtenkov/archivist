package ru.javaprojects.archivist.users.password_reset;

public class PasswordResetException extends RuntimeException{
    public PasswordResetException(String message) {
        super(message);
    }
}
