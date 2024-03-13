package ru.javaprojects.archivist.albums.to;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IssuanceTo {

    @NotNull
    private Long albumId;

    @NotNull
    private Long employeeId;

    @NotNull
    private LocalDate issued;

    @Override
    public String toString() {
        return String.format("IssuanceTo[albumId=%d, employeeId=%d, issued=%s]", albumId, employeeId, issued);
    }
}
