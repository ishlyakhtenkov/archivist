package ru.javaprojects.archivist.documents.model;

public enum Status {
    UNACCOUNTED_COPY("Unaccounted copy"), ACCOUNTED_COPY("Accounted copy"), DUPLICATE("Duplicate"), ORIGINAL("Original");

    private final String displayName;

    Status(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
