package ru.javaprojects.archivist.users.password_reset.mail;

public interface MailSender {
    void sendEmail(String to, String subject, String text);
}
