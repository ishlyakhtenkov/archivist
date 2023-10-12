package ru.javaprojects.archivist.documents.model;

public enum Type {
    PAPER("Paper"), DIGITAL("Digital");

    private final String displayName;

    Type(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
