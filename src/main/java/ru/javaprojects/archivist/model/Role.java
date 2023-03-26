package ru.javaprojects.archivist.model;

import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    USER,
    ARCHIVIST,
    ADMIN;

    @Override
    public String getAuthority() {
        return "ROLE_" + name();
    }
}
