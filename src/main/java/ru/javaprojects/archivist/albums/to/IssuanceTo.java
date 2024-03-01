package ru.javaprojects.archivist.albums.to;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class IssuanceTo {

    @NotNull
    private Long albumId;

    @NotNull
    private Long employeeId;

    @NotNull
    private LocalDate issued;
}
