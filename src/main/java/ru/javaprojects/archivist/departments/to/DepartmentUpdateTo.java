package ru.javaprojects.archivist.departments.to;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.archivist.common.HasIdAndName;
import ru.javaprojects.archivist.common.BaseTo;
import ru.javaprojects.archivist.common.util.validation.NoHtml;
import ru.javaprojects.archivist.departments.model.Employee;

@Getter
@Setter
@NoArgsConstructor
public class DepartmentUpdateTo extends BaseTo implements HasIdAndName {

    @NotBlank
    @NoHtml
    @Size(min = 2, max = 128)
    private String name;

    @NotNull
    private Employee boss;

    public DepartmentUpdateTo(Long id, String name, Employee boss) {
        super(id);
        this.name = name;
        this.boss = boss;
    }
}
