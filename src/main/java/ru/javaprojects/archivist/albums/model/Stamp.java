package ru.javaprojects.archivist.albums.model;

public enum Stamp {
    STAMP_1, STAMP_2, STAMP_5;

    @Override
    public String toString() {
        String[] split = name().split("_");
        return split[1] + " " + split[0].toLowerCase();
    }
}
