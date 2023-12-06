package ru.javaprojects.archivist.posts;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import ru.javaprojects.archivist.common.HasId;
import ru.javaprojects.archivist.common.model.BaseEntity;
import ru.javaprojects.archivist.common.util.validation.NoHtml;
import ru.javaprojects.archivist.users.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "posts")
@Getter
@Setter
@NoArgsConstructor
public class Post extends BaseEntity implements HasId {

    @NotBlank
    @NoHtml
    @Size(max = 512)
    @Column(name = "title", nullable = false)
    private String title;

    @NotBlank
    @NoHtml
    @Column(name = "content", nullable = false)
    private String content;

    @CreationTimestamp
    @Column(name = "created")
    private LocalDateTime created;

    @UpdateTimestamp
    @Column(name = "updated")
    private LocalDateTime updated;

    @Column(name = "for_auth_only", nullable = false, columnDefinition = "bool default true")
    private boolean forAuthOnly = true;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    public Post(Long id, String title, String content, boolean forAuthOnly) {
        super(id);
        this.title = title;
        this.content = content;
        this.forAuthOnly = forAuthOnly;
    }

    public Post(Long id, String title, String content, LocalDateTime created, LocalDateTime updated, boolean forAuthOnly, User author) {
        super(id);
        this.title = title;
        this.content = content;
        this.created = created;
        this.updated = updated;
        this.forAuthOnly = forAuthOnly;
        this.author = author;
    }
}
