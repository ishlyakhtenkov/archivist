package ru.javaprojects.archivist.users.password_reset.mail;

public class MailException extends RuntimeException {
    public MailException(String message) {
        super(message);
    }
}
