package ru.javaprojects.archivist.documents.model;

public enum Status {
    ORIGINAL("Original"), DUPLICATE("Duplicate"), ACCOUNTED_COPY("Accounted copy"), UNACCOUNTED_COPY("Unaccounted copy");

    private final String displayName;

    Status(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
