package ru.javaprojects.archivist.posts.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.MultiValueMap;
import ru.javaprojects.archivist.AbstractControllerTest;
import ru.javaprojects.archivist.common.error.NotFoundException;
import ru.javaprojects.archivist.posts.*;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.archivist.AbstractControllerTest.ExceptionResultMatchers.exception;
import static ru.javaprojects.archivist.CommonTestData.*;
import static ru.javaprojects.archivist.common.web.PathUIController.LOGIN_URL;
import static ru.javaprojects.archivist.posts.PostTestData.*;
import static ru.javaprojects.archivist.posts.web.PostUIController.POSTS_URL;

class PostUIControllerTest extends AbstractControllerTest {
    private static final String POSTS_ADD_FORM_URL = POSTS_URL + "/add";
    private static final String POSTS_EDIT_FORM_URL = POSTS_URL + "/edit/";

    private static final String POSTS_FORM_VIEW = "posts/post-form";

    @Autowired
    private PostService service;

    @Autowired
    private PostRepository repository;


    @Test
    @WithUserDetails(ADMIN_MAIL)
    void showAddForm() throws Exception {
        perform(MockMvcRequestBuilders.get(POSTS_ADD_FORM_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(POST_TO_ATTRIBUTE))
                .andExpect(view().name(POSTS_FORM_VIEW))
                .andExpect(result ->
                        POST_TO_MATCHER.assertMatch((PostTo) Objects.requireNonNull(result.getModelAndView()).getModel().get(POST_TO_ATTRIBUTE),
                                new PostTo()));
    }

    @Test
    void showAddFormUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(POSTS_ADD_FORM_URL))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void showAddFormForbiddenWhenArchivist() throws Exception {
        perform(MockMvcRequestBuilders.get(POSTS_ADD_FORM_URL))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showAddFormForbiddenWhenUser() throws Exception {
        perform(MockMvcRequestBuilders.get(POSTS_ADD_FORM_URL))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void create() throws Exception {
        Post newPost = PostTestData.getNew();
        perform(MockMvcRequestBuilders.post(POSTS_URL)
                .params((PostTestData.getNewParams()))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute(ACTION, "Post " + newPost.getTitle() + " was created"));
        Post created = repository.findAllByTitle(newPost.getTitle()).get(0);
        newPost.setId(created.id());
        POST_MATCHER.assertMatch(created, newPost);
    }

    @Test
    void createUnAuthorize() throws Exception {
        perform(MockMvcRequestBuilders.post(POSTS_URL)
                .params((PostTestData.getNewParams()))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertTrue(repository.findAllByTitle(PostTestData.getNew().getTitle()).isEmpty());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createForbidden() throws Exception {
        perform(MockMvcRequestBuilders.post(POSTS_URL)
                .params((PostTestData.getNewParams()))
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertTrue(repository.findAllByTitle(PostTestData.getNew().getTitle()).isEmpty());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void createInvalid() throws Exception {
        MultiValueMap<String, String> newInvalidParams = PostTestData.getNewInvalidParams();
        perform(MockMvcRequestBuilders.post(POSTS_URL)
                .params(newInvalidParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(POST_TO_ATTRIBUTE, TITLE_PARAM, CONTENT_PARAM))
                .andExpect(view().name(POSTS_FORM_VIEW));
        assertTrue(repository.findAllByTitle(newInvalidParams.get(TITLE_PARAM).get(0)).isEmpty());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void showEditForm() throws Exception {
        perform(MockMvcRequestBuilders.get(POSTS_EDIT_FORM_URL + POST1_ID))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(POST_TO_ATTRIBUTE))
                .andExpect(view().name(POSTS_FORM_VIEW))
                .andExpect(result ->
                        POST_TO_MATCHER.assertMatch((PostTo) Objects.requireNonNull(result.getModelAndView()).getModel().get(POST_TO_ATTRIBUTE), PostUtil.asTo(post1)));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void showEditFormNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(POSTS_EDIT_FORM_URL + NOT_FOUND))
                .andExpect(exception().exceptionPage(ENTITY_NOT_FOUND, NotFoundException.class));
    }

    @Test
    void showEditFormUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(POSTS_EDIT_FORM_URL + POST1_ID))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showEditFormForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(POSTS_EDIT_FORM_URL + POST1_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void update() throws Exception {
        Post updatedPost = PostTestData.getUpdated();
        perform(MockMvcRequestBuilders.post(POSTS_URL)
                .params(PostTestData.getUpdatedParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"))
                .andExpect(flash().attribute(ACTION, "Post " + updatedPost.getTitle() + " was updated"));
        POST_MATCHER.assertMatch(repository.findById(POST1_ID).orElseThrow(), updatedPost);
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateNotFound() throws Exception {
        MultiValueMap<String, String> updatedParams = PostTestData.getUpdatedParams();
        updatedParams.set(ID, NOT_FOUND + "");
        perform(MockMvcRequestBuilders.post(POSTS_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(exception().exceptionPage(ENTITY_NOT_FOUND, NotFoundException.class));
    }

    @Test
    void updateUnAuthorize() throws Exception {
        perform(MockMvcRequestBuilders.post(POSTS_URL)
                .params(PostTestData.getUpdatedParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertNotEquals(service.get(POST1_ID).getTitle(), PostTestData.getUpdated().getTitle());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateForbidden() throws Exception {
        perform(MockMvcRequestBuilders.post(POSTS_URL)
                .params(PostTestData.getUpdatedParams())
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertNotEquals(service.get(POST1_ID).getTitle(), PostTestData.getUpdated().getTitle());
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    void updateInvalid() throws Exception {
        perform(MockMvcRequestBuilders.post(POSTS_URL)
                .params(PostTestData.getUpdatedInvalidParams())
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(POST_TO_ATTRIBUTE, TITLE_PARAM, CONTENT_PARAM))
                .andExpect(view().name(POSTS_FORM_VIEW));
        assertNotEquals(service.get(POST1_ID).getTitle(), PostTestData.getUpdated().getTitle());
    }
}
