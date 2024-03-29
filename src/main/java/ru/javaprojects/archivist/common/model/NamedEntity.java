package ru.javaprojects.archivist.common.model;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.archivist.common.util.validation.NoHtml;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class NamedEntity extends BaseEntity {

    @NotBlank
    @NoHtml
    @Size(min = 2, max = 128)
    @Column(name = "name", nullable = false)
    protected String name;

    protected NamedEntity(Long id, String name) {
        super(id);
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format(getClass().getSimpleName() + "[id=%d, name=%s]", id, name);
    }
}
