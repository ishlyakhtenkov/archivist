package ru.javaprojects.archivist.departments.to;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.archivist.common.HasIdAndName;
import ru.javaprojects.archivist.common.BaseTo;
import ru.javaprojects.archivist.common.util.validation.NoHtml;

@Getter
@Setter
@NoArgsConstructor
public class DepartmentCreateTo extends BaseTo implements HasIdAndName {

    @NotBlank
    @NoHtml
    @Size(min = 2, max = 128)
    private String name;

    @NotBlank
    @NoHtml
    @Size(min = 2, max = 32)
    private String bossLastName;

    @NotBlank
    @NoHtml
    @Size(min = 2, max = 32)
    private String bossFirstName;

    @NotBlank
    @NoHtml
    @Size(min = 2, max = 32)
    private String bossMiddleName;

    @NotBlank
    @NoHtml
    @Size(max = 32)
    private String bossPhone;

    @Email
    @NoHtml
    @Size(max = 128)
    @Nullable
    private String bossEmail;

    @Override
    public String toString() {
        return "Department: " + name;
    }
}
