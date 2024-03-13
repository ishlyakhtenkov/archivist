package ru.javaprojects.archivist.posts;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.javaprojects.archivist.common.to.BaseTo;
import ru.javaprojects.archivist.common.util.validation.NoHtml;

@Getter
@Setter
@NoArgsConstructor
public class PostTo extends BaseTo {

    @NotBlank
    @NoHtml
    @Size(max = 512)
    private String title;

    @NotBlank
    @NoHtml
    private String content;

    private boolean forAuthOnly = true;

    public PostTo(Long id, String title, String content, boolean forAuthOnly) {
        super(id);
        this.title = title;
        this.content = content;
        this.forAuthOnly = forAuthOnly;
    }

    @Override
    public String toString() {
        return String.format("PostTo[id=%d, title=%s]", id, title);
    }
}
