package ru.javaprojects.archivist.common.error;

public class DataConflictException extends RuntimeException{
    public DataConflictException(String message) {
        super(message);
    }
}
