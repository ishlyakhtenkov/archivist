package ru.javaprojects.archivist.posts;

import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.javaprojects.archivist.MatcherFactory;

import java.time.LocalDateTime;

import static java.time.Month.DECEMBER;
import static java.time.Month.NOVEMBER;
import static ru.javaprojects.archivist.CommonTestData.ID;
import static ru.javaprojects.archivist.users.UserTestData.admin;

public class PostTestData {
    public static final MatcherFactory.Matcher<Post> POST_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(Post.class, "created", "updated", "author.roles",
                    "author.password", "author.registered");

    public static final MatcherFactory.Matcher<PostTo> POST_TO_MATCHER =
            MatcherFactory.usingIgnoringFieldsComparator(PostTo.class);

    public static final long POST1_ID = 100054;
    public static final long POST2_ID = 100055;
    public static final long POST3_ID = 100056;

    public static final String POST_TO_ATTRIBUTE = "postTo";
    public static final String TITLE = "title";
    public static final String CONTENT = "content";
    public static final String FOR_AUTH_ONLY = "forAuthOnly";

    public static final Post post1 = new Post(POST1_ID, "New users registration",
            "To apply for an account, please call 1-134-56 or email ishlyakhtenkov@npo.lan.",
            LocalDateTime.of(2023, NOVEMBER, 28, 11, 14, 48), LocalDateTime.of(2023, NOVEMBER, 28, 11, 14, 48), false, admin);

    public static final Post post2 = new Post(POST2_ID, "Account sharing",
            "Dear users, remind you that sharing your account with other users is strictly prohibited.",
            LocalDateTime.of(2023, NOVEMBER, 30, 15, 32, 25), LocalDateTime.of(2023, NOVEMBER, 30, 15, 32, 25), true, admin);

    public static final Post post3 = new Post(POST3_ID, "Server maintenance",
            "Dear users, 05.12.2023 from 10:00 to 12:00 AM the app will be unavailable due to technical works",
            LocalDateTime.of(2023, DECEMBER, 4, 9, 5, 34), LocalDateTime.of(2023, DECEMBER, 4, 9, 5, 34), true, admin);

    public static Post getNew() {
        return new Post(null, "New post title", "New post content", null, null, true, admin);
    }

    public static Post getUpdated() {
        return new Post(POST1_ID, "Updated post title", "Updated post content", null, null, false, admin);
    }

    public static MultiValueMap<String, String> getNewParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        Post newPost = getNew();
        params.add(TITLE, newPost.getTitle());
        params.add(CONTENT, newPost.getContent());
        params.add(FOR_AUTH_ONLY, String.valueOf(newPost.isForAuthOnly()));
        return params;
    }

    public static MultiValueMap<String, String> getNewInvalidParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add(TITLE, "<p>New title</p>");
        params.add(CONTENT, "");
        params.add(FOR_AUTH_ONLY, String.valueOf(true));
        return params;
    }

    public static MultiValueMap<String, String> getUpdatedParams() {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        Post updatedPost = getUpdated();
        params.add(ID, String.valueOf(POST1_ID));
        params.add(TITLE, updatedPost.getTitle());
        params.add(CONTENT, updatedPost.getContent());
        params.add(FOR_AUTH_ONLY, String.valueOf(updatedPost.isForAuthOnly()));
        return params;
    }

    public static MultiValueMap<String, String> getUpdatedInvalidParams() {
        MultiValueMap<String, String> params = getNewInvalidParams();
        params.add(ID, String.valueOf(POST1_ID));
        return params;
    }
}
