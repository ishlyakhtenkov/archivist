package ru.javaprojects.archivist.documents.model;


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
    @Column(name = "file_name")
    private String fileName;

    @NotBlank
    @Size(min = 2, max = 512)
    @Column(name = "file_link")
    private String fileLink;
}
