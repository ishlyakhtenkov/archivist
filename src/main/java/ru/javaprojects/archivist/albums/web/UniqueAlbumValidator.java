package ru.javaprojects.archivist.albums.web;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import ru.javaprojects.archivist.albums.repository.AlbumRepository;
import ru.javaprojects.archivist.albums.to.AlbumTo;

import java.util.Objects;

import static ru.javaprojects.archivist.common.error.Constants.DUPLICATE_ERROR_CODE;

@Component
@AllArgsConstructor
public class UniqueAlbumValidator implements org.springframework.validation.Validator {
    private final AlbumRepository repository;

    @Override
    public boolean supports(Class<?> clazz) {
        return AlbumTo.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        AlbumTo albumTo = (AlbumTo) target;
        if (StringUtils.hasText(albumTo.getDecimalNumber())) {
            repository.findByMainDocument_DecimalNumberIgnoreCaseAndStamp(albumTo.getDecimalNumber(), albumTo.getStamp())
                    .ifPresent(dbAlbum -> {
                        if (albumTo.isNew() || !Objects.equals(albumTo.getId(), dbAlbum.getId())) {
                            errors.rejectValue("decimalNumber", DUPLICATE_ERROR_CODE, "Album with these decimal number and stamp already exists");
                        }
                    });
        }
    }
}
