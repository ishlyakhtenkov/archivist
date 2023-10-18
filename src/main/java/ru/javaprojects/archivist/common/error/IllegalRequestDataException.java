package ru.javaprojects.archivist.common.error;

public class IllegalRequestDataException extends RuntimeException {
    public IllegalRequestDataException(String message) {
        super(message);
    }
}
