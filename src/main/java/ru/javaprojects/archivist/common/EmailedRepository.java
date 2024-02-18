package ru.javaprojects.archivist.common;

import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

@NoRepositoryBean
public interface EmailedRepository<T> extends BaseRepository<T> {
    Optional<T> findByEmailIgnoreCase(String email);
}
