package ru.javaprojects.archivist.posts;

import lombok.experimental.UtilityClass;

@UtilityClass
public class PostUtil {

    public static Post createNewFromTo(PostTo postTo) {
        return new Post(null, postTo.getTitle(), postTo.getContent(), postTo.isForAuthOnly());
    }

    public static PostTo asTo(Post post) {
        return new PostTo(post.getId(), post.getTitle(), post.getContent(), post.isForAuthOnly());
    }

    public static Post updateFromTo(Post post, PostTo postTo) {
        post.setTitle(postTo.getTitle());
        post.setContent(postTo.getContent());
        post.setForAuthOnly(postTo.isForAuthOnly());
        return post;
    }
}
