package ru.javaprojects.archivist.common;

import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

@NoRepositoryBean
public interface NamedRepository<T> extends BaseRepository<T> {
    Optional<T> findByNameIgnoreCase(String name);
}
