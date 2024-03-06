package ru.javaprojects.archivist.albums.web;

import org.hibernate.Hibernate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.MultiValueMap;
import ru.javaprojects.archivist.AbstractControllerTest;
import ru.javaprojects.archivist.albums.model.Album;
import ru.javaprojects.archivist.albums.model.Issuance;
import ru.javaprojects.archivist.albums.model.Stamp;
import ru.javaprojects.archivist.albums.AlbumService;
import ru.javaprojects.archivist.common.error.NotFoundException;
import ru.javaprojects.archivist.departments.model.Employee;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.archivist.AbstractControllerTest.ExceptionResultMatchers.exception;
import static ru.javaprojects.archivist.CommonTestData.*;
import static ru.javaprojects.archivist.albums.AlbumTestData.*;
import static ru.javaprojects.archivist.albums.web.AlbumUIController.ALBUMS_URL;
import static ru.javaprojects.archivist.common.error.Constants.DUPLICATE_ERROR_CODE;
import static ru.javaprojects.archivist.common.web.PathUIController.LOGIN_URL;
import static ru.javaprojects.archivist.documents.DocumentTestData.DECIMAL_NUMBER;

class AlbumUIControllerTest extends AbstractControllerTest {
    private static final String ALBUMS_ADD_FORM_URL = ALBUMS_URL + "/add";
    private static final String ALBUMS_EDIT_FORM_URL = ALBUMS_URL + "/edit/";
    static final String ALBUMS_URL_SLASH = ALBUMS_URL + "/";
    private static final String ALBUMS_VIEW = "albums/albums";
    private static final String ALBUM_VIEW = "albums/album";
    private static final String ALBUMS_FORM_VIEW = "albums/album-form";

    @Autowired
    private AlbumService service;

    @Test
    @WithUserDetails(USER_MAIL)
    @SuppressWarnings("unchecked")
    void getAll() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(ALBUMS_URL)
                .params(getPageableParams()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(ALBUMS_ATTRIBUTE))
                .andExpect(view().name(ALBUMS_VIEW));
        Page<Album> albums = (Page<Album>) Objects.requireNonNull(actions.andReturn().getModelAndView())
                .getModel().get(ALBUMS_ATTRIBUTE);
        assertEquals(4, albums.getTotalElements());
        assertEquals(2, albums.getTotalPages());
        ALBUM_MATCHER.assertMatchIgnoreFields(albums.getContent(), List.of(album4, album1), "mainDocument.developer",
                "mainDocument.originalHolder", "issuances");
        assertTrue(albums.getContent().get(0).getIssuances().isEmpty());

        assertEquals(1, albums.getContent().get(1).getIssuances().size());
        Issuance lastIssuance = albums.getContent().get(1).getIssuances().get(0);
        lastIssuance.setEmployee(Hibernate.unproxy(lastIssuance.getEmployee(), Employee.class));
        ISSUANCE_MATCHER.assertMatchIgnoreFields(lastIssuance, album1Issuance5,
                "album.mainDocument", "album.issuances", "employee.department");
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    @SuppressWarnings("unchecked")
    void getAllByKeyword() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(ALBUMS_URL)
                .param(KEYWORD, "upia"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(ALBUMS_ATTRIBUTE))
                .andExpect(view().name(ALBUMS_VIEW));
        Page<Album> albums = (Page<Album>) Objects.requireNonNull(actions.andReturn().getModelAndView())
                .getModel().get(ALBUMS_ATTRIBUTE);
        assertEquals(1, albums.getTotalElements());
        assertEquals(1, albums.getTotalPages());
        ALBUM_MATCHER.assertMatchIgnoreFields(albums.getContent(), List.of(album4), "mainDocument.developer",
                "mainDocument.originalHolder", "issuances");
        assertTrue(albums.getContent().get(0).getIssuances().isEmpty());
    }

    @Test
    void getAllUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(ALBUMS_URL)
                .params(getPageableParams()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void get() throws Exception {
        perform(MockMvcRequestBuilders.get(ALBUMS_URL_SLASH + ALBUM1_ID))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(ALBUM_ATTRIBUTE))
                .andExpect(view().name(ALBUM_VIEW))
                .andExpect(result -> ALBUM_MATCHER.assertMatchIgnoreFields((Album) Objects.requireNonNull(result
                        .getModelAndView()).getModel().get(ALBUM_ATTRIBUTE), album1, "mainDocument.developer",
                        "mainDocument.originalHolder", "issuances.employee.department", "issuances.album"));
    }

    @Test
    void getUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(ALBUMS_URL_SLASH + ALBUM1_ID))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void showAddForm() throws Exception {
        perform(MockMvcRequestBuilders.get(ALBUMS_ADD_FORM_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(ALBUM_TO_ATTRIBUTE))
                .andExpect(model().attributeExists(STAMPS_ATTRIBUTE))
                .andExpect(view().name(ALBUMS_FORM_VIEW));
    }

    @Test
    void showAddFormUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(ALBUMS_ADD_FORM_URL))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showAddFormForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(ALBUMS_ADD_FORM_URL))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void create() throws Exception {
        Album newAlbum = getNewAlbum();
        perform(MockMvcRequestBuilders.post(ALBUMS_URL)
                .params(getNewAlbumParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(ALBUMS_URL))
                .andExpect(flash().attribute(ACTION, "Album " + newAlbum.getMainDocument().getDecimalNumber() + " was created"));
        Album created = service.getByDecimalNumberAndStamp(newAlbum.getMainDocument().getDecimalNumber(), newAlbum.getStamp());
        newAlbum.setId(created.id());
        ALBUM_MATCHER.assertMatchIgnoreFields(created, newAlbum, "mainDocument.developer", "mainDocument.originalHolder",
                "issuances");
    }

    @Test
    void createUnAuthorize() throws Exception {
        perform(MockMvcRequestBuilders.post(ALBUMS_URL)
                .params(getNewAlbumParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        Album newAlbum = getNewAlbum();
        assertThrows(NotFoundException.class,
                () -> service.getByDecimalNumberAndStamp(newAlbum.getMainDocument().getDecimalNumber(), newAlbum.getStamp()));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createForbidden() throws Exception {
        perform(MockMvcRequestBuilders.post(ALBUMS_URL)
                .params(getNewAlbumParams())
                .with(csrf()))
                .andExpect(status().isForbidden());
        Album newAlbum = getNewAlbum();
        assertThrows(NotFoundException.class,
                () -> service.getByDecimalNumberAndStamp(newAlbum.getMainDocument().getDecimalNumber(), newAlbum.getStamp()));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createInvalid() throws Exception {
        MultiValueMap<String, String> newInvalidParams = getNewAlbumInvalidParams();
        perform(MockMvcRequestBuilders.post(ALBUMS_URL)
                .params(newInvalidParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(ALBUM_TO_ATTRIBUTE, DECIMAL_NUMBER))
                .andExpect(view().name(ALBUMS_FORM_VIEW));
        assertThrows(NotFoundException.class,
                () -> service.getByDecimalNumberAndStamp(newInvalidParams.get(DECIMAL_NUMBER).get(0),
                        Stamp.valueOf(newInvalidParams.get(STAMP).get(0))));

    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createDuplicateDecimalNumberAndStamp() throws Exception {
        MultiValueMap<String, String> newParams = getNewAlbumParams();
        newParams.set(DECIMAL_NUMBER, album1.getMainDocument().getDecimalNumber());
        perform(MockMvcRequestBuilders.post(ALBUMS_URL)
                .params(newParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(ALBUM_TO_ATTRIBUTE, DECIMAL_NUMBER, DUPLICATE_ERROR_CODE))
                .andExpect(view().name(ALBUMS_FORM_VIEW));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void showEditForm() throws Exception {
        perform(MockMvcRequestBuilders.get(ALBUMS_EDIT_FORM_URL + ALBUM1_ID))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(ALBUM_TO_ATTRIBUTE))
                .andExpect(model().attributeExists(STAMPS_ATTRIBUTE))
                .andExpect(view().name(ALBUMS_FORM_VIEW));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void showEditFormNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(ALBUMS_EDIT_FORM_URL + NOT_FOUND))
                .andExpect(exception().exceptionPage(ENTITY_NOT_FOUND, NotFoundException.class));
    }

    @Test
    void showEditFormUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(ALBUMS_EDIT_FORM_URL + ALBUM1_ID))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showEditFormForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(ALBUMS_EDIT_FORM_URL + ALBUM1_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void update() throws Exception {
        Album updatedAlbum = getUpdatedAlbum();
        perform(MockMvcRequestBuilders.post(ALBUMS_URL)
                .params(getUpdatedAlbumParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(ALBUMS_URL))
                .andExpect(flash().attribute(ACTION, "Album " + updatedAlbum.getMainDocument().getDecimalNumber() +
                        " was updated"));
        ALBUM_MATCHER.assertMatchIgnoreFields(service.getWithIssuances(ALBUM3_ID), updatedAlbum,
                "mainDocument.developer", "mainDocument.originalHolder", "issuances");
    }

    //Check UniqueAlbumValidator works correct when update
    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void updateDecimalNumberAndStampNotChange() throws Exception {
        Album updatedAlbum = getUpdatedAlbum();
        updatedAlbum.setStamp(album3.getStamp());
        MultiValueMap<String, String> updatedParams = getUpdatedAlbumParams();
        updatedParams.set(STAMP, album3.getStamp().name());
        perform(MockMvcRequestBuilders.post(ALBUMS_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(ALBUMS_URL))
                .andExpect(flash().attribute(ACTION, "Album " + updatedAlbum.getMainDocument().getDecimalNumber() +
                        " was updated"));
        ALBUM_MATCHER.assertMatchIgnoreFields(service.getWithIssuances(ALBUM3_ID), updatedAlbum,
                "mainDocument.developer", "mainDocument.originalHolder", "issuances");
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void updateNotFound() throws Exception {
        MultiValueMap<String, String> updatedParams = getUpdatedAlbumParams();
        updatedParams.set(ID, String.valueOf(NOT_FOUND));
        perform(MockMvcRequestBuilders.post(ALBUMS_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(exception().exceptionPage(ENTITY_NOT_FOUND, NotFoundException.class));
    }

    @Test
    void updateUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.post(ALBUMS_URL)
                .params(getUpdatedAlbumParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertNotEquals(service.get(ALBUM3_ID).getStamp(), getUpdatedAlbum().getStamp());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateForbidden() throws Exception {
        perform(MockMvcRequestBuilders.post(ALBUMS_URL)
                .params(getUpdatedAlbumParams())
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertNotEquals(service.get(ALBUM3_ID).getStamp(), getUpdatedAlbum().getStamp());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void updateInvalid() throws Exception {
        MultiValueMap<String, String> updatedInvalidParams = getUpdatedAlbumInvalidParams();
        perform(MockMvcRequestBuilders.post(ALBUMS_URL)
                .params(updatedInvalidParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(ALBUM_TO_ATTRIBUTE, DECIMAL_NUMBER))
                .andExpect(view().name(ALBUMS_FORM_VIEW));
        assertNotEquals(service.getWithIssuances(ALBUM3_ID).getMainDocument().getDecimalNumber(),
                updatedInvalidParams.get(DECIMAL_NUMBER).get(0));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void updateDuplicateDecimalNumberAndStamp() throws Exception {
        MultiValueMap<String, String> updatedParams = getUpdatedAlbumParams();
        updatedParams.set(DECIMAL_NUMBER, album1.getMainDocument().getDecimalNumber());
        perform(MockMvcRequestBuilders.post(ALBUMS_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(ALBUM_TO_ATTRIBUTE, DECIMAL_NUMBER, DUPLICATE_ERROR_CODE))
                .andExpect(view().name(ALBUMS_FORM_VIEW));
        assertNotEquals(service.getWithIssuances(ALBUM3_ID).getMainDocument().getDecimalNumber(),
                album1.getMainDocument().getDecimalNumber());
    }
}
