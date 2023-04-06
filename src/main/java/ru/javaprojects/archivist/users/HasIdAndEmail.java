package ru.javaprojects.archivist.users;

import ru.javaprojects.archivist.common.HasId;

public interface HasIdAndEmail extends HasId {
    String getEmail();
}
