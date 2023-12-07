package ru.javaprojects.archivist.common.web;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaprojects.archivist.AbstractControllerTest;
import ru.javaprojects.archivist.posts.Post;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.archivist.CommonTestData.*;
import static ru.javaprojects.archivist.common.web.PathUIController.LOGIN_URL;
import static ru.javaprojects.archivist.posts.PostTestData.*;

class PathUIControllerTest extends AbstractControllerTest {
    private static final String POSTS_ATTRIBUTE = "posts";
    private static final String LOGIN_PAGE_VIEW = "users/login";
    private static final String HOME_PAGE_VIEW = "index";

    @Test
    void showLoginPageUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(LOGIN_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(LOGIN_PAGE_VIEW));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showLoginPageAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(LOGIN_URL))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(HOME_URL));
    }

    @Test
    @SuppressWarnings("unchecked")
    void showHomePageUnAuthorized() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(HOME_URL)
                .params(getPageableParams()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(POSTS_ATTRIBUTE))
                .andExpect(view().name(HOME_PAGE_VIEW));
        Page<Post> posts = (Page<Post>) Objects.requireNonNull(actions.andReturn().getModelAndView())
                .getModel().get(POSTS_ATTRIBUTE);
        assertEquals(1, posts.getTotalElements());
        assertEquals(1, posts.getTotalPages());
        POST_MATCHER.assertMatch(posts.getContent(), List.of(post1));
    }

    @Test
    @SuppressWarnings("unchecked")
    @WithUserDetails(USER_MAIL)
    void showHomePage() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(HOME_URL)
                .params(getPageableParams()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(POSTS_ATTRIBUTE))
                .andExpect(view().name(HOME_PAGE_VIEW));
        Page<Post> posts = (Page<Post>) Objects.requireNonNull(actions.andReturn().getModelAndView())
                .getModel().get(POSTS_ATTRIBUTE);
        assertEquals(3, posts.getTotalElements());
        assertEquals(2, posts.getTotalPages());
        POST_MATCHER.assertMatch(posts.getContent(), List.of(post3, post2));
    }
}
