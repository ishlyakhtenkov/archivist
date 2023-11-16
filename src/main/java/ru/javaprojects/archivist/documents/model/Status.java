package ru.javaprojects.archivist.documents.model;

import org.springframework.util.StringUtils;

public enum Status {
    UNACCOUNTED_COPY, ACCOUNTED_COPY, DUPLICATE, ORIGINAL;

    @Override
    public String toString() {
        return StringUtils.capitalize(name().replace('_', ' ').toLowerCase());
    }
}
