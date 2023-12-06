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
import static ru.javaprojects.archivist.CommonTestData.USER_MAIL;
import static ru.javaprojects.archivist.CommonTestData.getPageableParams;
import static ru.javaprojects.archivist.common.web.PathUIController.LOGIN_URL;
import static ru.javaprojects.archivist.posts.PostTestData.*;

class PathUIControllerTest extends AbstractControllerTest {

    @Test
    void showLoginPageUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(LOGIN_URL))
                .andExpect(status().isOk())
                .andExpect(view().name("users/login"));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showLoginPageAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(LOGIN_URL))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void showHomePageUnAuthorized() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get("/")
                .params(getPageableParams()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("posts"))
                .andExpect(view().name("index"));
        Page<Post> posts = (Page<Post>) Objects.requireNonNull(actions.andReturn().getModelAndView())
                .getModel().get("posts");
        assertEquals(1, posts.getTotalElements());
        assertEquals(1, posts.getTotalPages());
        POST_MATCHER.assertMatch(posts.getContent(), List.of(post1));
    }

    @Test
    @SuppressWarnings("unchecked")
    @WithUserDetails(USER_MAIL)
    void showHomePage() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get("/")
                .params(getPageableParams()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("posts"))
                .andExpect(view().name("index"));
        Page<Post> posts = (Page<Post>) Objects.requireNonNull(actions.andReturn().getModelAndView())
                .getModel().get("posts");
        assertEquals(3, posts.getTotalElements());
        assertEquals(2, posts.getTotalPages());
        POST_MATCHER.assertMatch(posts.getContent(), List.of(post3, post2));
    }
}
