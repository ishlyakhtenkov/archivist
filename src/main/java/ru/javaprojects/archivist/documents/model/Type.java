package ru.javaprojects.archivist.documents.model;

import org.springframework.util.StringUtils;

public enum Type {
    PAPER, DIGITAL;

    @Override
    public String toString() {
        return StringUtils.capitalize(name().toLowerCase());
    }
}
