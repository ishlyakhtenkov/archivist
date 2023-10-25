package ru.javaprojects.archivist.documents.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.javaprojects.archivist.common.util.validation.NoHtml;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class ContentFile {

    @NotBlank
    @NoHtml
    @Size(min = 2, max = 128)
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank
    @Size(min = 2, max = 512)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Column(name = "file_link", nullable = false)
    private String fileLink;
}
