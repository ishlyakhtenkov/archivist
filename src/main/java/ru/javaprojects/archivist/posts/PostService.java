package ru.javaprojects.archivist.posts;

import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import ru.javaprojects.archivist.users.AuthUser;

import static ru.javaprojects.archivist.posts.PostUtil.createNewFromTo;
import static ru.javaprojects.archivist.posts.PostUtil.updateFromTo;

@Service
@AllArgsConstructor
public class PostService {
    private final PostRepository repository;

    public Page<Post> getAll(Pageable pageable, boolean isAuthenticated) {
        return isAuthenticated ? repository.findAllByOrderByCreatedDesc(pageable) :
                repository.findAllByForAuthOnlyIsFalseOrderByCreatedDesc(pageable);
    }

    public Post get(long id) {
        return repository.getExisted(id);
    }

    public void create(PostTo postTo) {
        Assert.notNull(postTo, "postTo must not be null");
        Post post = createNewFromTo(postTo);
        post.setAuthor(AuthUser.authUser());
        repository.save(post);
    }

    @Transactional
    public void update(PostTo postTo) {
        Assert.notNull(postTo, "postTo must not be null");
        Post post = get(postTo.id());
        updateFromTo(post, postTo);
        post.setAuthor(AuthUser.authUser());
    }

    public void delete(long id) {
        repository.deleteExisted(id);
    }
}
