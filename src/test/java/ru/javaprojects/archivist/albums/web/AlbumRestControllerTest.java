package ru.javaprojects.archivist.albums.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaprojects.archivist.AbstractControllerTest;
import ru.javaprojects.archivist.albums.model.Issuance;
import ru.javaprojects.archivist.albums.repository.IssuanceRepository;
import ru.javaprojects.archivist.albums.AlbumService;
import ru.javaprojects.archivist.albums.to.IssuanceTo;
import ru.javaprojects.archivist.common.error.IllegalRequestDataException;
import ru.javaprojects.archivist.common.error.NotFoundException;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.javaprojects.archivist.CommonTestData.*;
import static ru.javaprojects.archivist.albums.AlbumTestData.*;
import static ru.javaprojects.archivist.albums.web.AlbumUIControllerTest.ALBUMS_URL_SLASH;
import static ru.javaprojects.archivist.common.util.JsonUtil.writeValue;
import static ru.javaprojects.archivist.common.web.PathUIController.LOGIN_URL;

class AlbumRestControllerTest extends AbstractControllerTest {
    private static final String ALBUM_ISSUANCES_URL = ALBUMS_URL_SLASH + "%d/issuances";
    private static final String ALBUM_ISSUE_URL = ALBUMS_URL_SLASH + "%d/issue";
    private static final String ALBUM_RETURN_URL = ALBUMS_URL_SLASH + "%d/return";
    private static final String ISSUANCES_URL_SLASH = ALBUMS_URL_SLASH + "issuances/";

    @Autowired
    private AlbumService service;

    @Autowired
    private IssuanceRepository issuanceRepository;

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void delete() throws Exception {
        perform(MockMvcRequestBuilders.delete(ALBUMS_URL_SLASH + ALBUM1_ID)
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertThrows(NotFoundException.class, () -> service.get(ALBUM1_ID));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void deleteNotFound() throws Exception {
        perform(MockMvcRequestBuilders.delete(ALBUMS_URL_SLASH + NOT_FOUND)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(ALBUMS_URL_SLASH + NOT_FOUND));
    }

    @Test
    void deleteUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.delete(ALBUMS_URL_SLASH + ALBUM1_ID)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertDoesNotThrow(() -> service.get(ALBUM1_ID));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void deleteForbidden() throws Exception {
        perform(MockMvcRequestBuilders.delete(ALBUMS_URL_SLASH + ALBUM1_ID)
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertDoesNotThrow(() -> service.get(ALBUM1_ID));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void getIssuances() throws Exception {
        perform(MockMvcRequestBuilders.get(String.format(ALBUM_ISSUANCES_URL, ALBUM1_ID))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(ISSUANCE_MATCHER.contentJsonIgnoreFields(List.of(album1Issuance5, album1Issuance4,
                        album1Issuance3, album1Issuance2, album1Issuance1), "album", "employee.department"));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void getIssuancesWhenAlbumNotExists() throws Exception {
        perform(MockMvcRequestBuilders.get(String.format(ALBUM_ISSUANCES_URL, NOT_FOUND))
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(String.format(ALBUM_ISSUANCES_URL, NOT_FOUND)));
    }

    @Test
    void getIssuancesUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(String.format(ALBUM_ISSUANCES_URL, ALBUM1_ID))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void deleteIssuance() throws Exception {
        perform(MockMvcRequestBuilders.delete(ISSUANCES_URL_SLASH + ALBUM1_ISSUANCE1_ID)
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertThrows(NotFoundException.class, () -> issuanceRepository.getExisted(ALBUM1_ISSUANCE1_ID));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void deleteIssuanceNotFound() throws Exception {
        perform(MockMvcRequestBuilders.delete(ISSUANCES_URL_SLASH + NOT_FOUND)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(ISSUANCES_URL_SLASH + NOT_FOUND));
    }

    @Test
    void deleteIssuanceUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.delete(ISSUANCES_URL_SLASH + ALBUM1_ISSUANCE1_ID)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertDoesNotThrow(() -> issuanceRepository.getExisted(ALBUM1_ISSUANCE1_ID));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void deleteIssuanceForbidden() throws Exception {
        perform(MockMvcRequestBuilders.delete(ISSUANCES_URL_SLASH + ALBUM1_ISSUANCE1_ID)
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertDoesNotThrow(() -> issuanceRepository.getExisted(ALBUM1_ISSUANCE1_ID));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void returnAlbum() throws Exception {
        perform(MockMvcRequestBuilders.patch(String.format(ALBUM_RETURN_URL, ALBUM1_ID))
                .param(RETURNED, CORRECT_RETURN_DATE.toString())
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertThrows(NotFoundException.class, () -> issuanceRepository.findByAlbum_IdAndReturnedIsNull(ALBUM1_ID)
                .orElseThrow(() -> new NotFoundException("Not found unreturned issue for album")));
        assertEquals(CORRECT_RETURN_DATE, issuanceRepository.getExisted(ALBUM1_ISSUANCE5_ID).getReturned());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void returnAlbumWithFutureReturnDate() throws Exception {
        perform(MockMvcRequestBuilders.patch(String.format(ALBUM_RETURN_URL, ALBUM1_ID))
                .param(RETURNED, LocalDate.now().plusDays(5).toString())
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        IllegalRequestDataException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail("Return date can not be greater than today date"))
                .andExpect(problemInstance(String.format(ALBUM_RETURN_URL, ALBUM1_ID)));
        assertNull(issuanceRepository.getExisted(ALBUM1_ISSUANCE5_ID).getReturned());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void returnAlbumWithReturnDateBeforeIssueDate() throws Exception {
        perform(MockMvcRequestBuilders.patch(String.format(ALBUM_RETURN_URL, ALBUM1_ID))
                .param(RETURNED, RETURN_DATE_BEFORE_ISSUE_DATE.toString())
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        IllegalRequestDataException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail("Return date must be greater than issue date"))
                .andExpect(problemInstance(String.format(ALBUM_RETURN_URL, ALBUM1_ID)));
        assertNull(issuanceRepository.getExisted(ALBUM1_ISSUANCE5_ID).getReturned());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void returnArchiveAlbum() throws Exception {
        perform(MockMvcRequestBuilders.patch(String.format(ALBUM_RETURN_URL, ALBUM2_ID))
                .param(RETURNED, CORRECT_RETURN_DATE.toString())
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        IllegalRequestDataException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail("Album is already in the Archive"))
                .andExpect(problemInstance(String.format(ALBUM_RETURN_URL, ALBUM2_ID)));
        assertThrows(NotFoundException.class, () -> issuanceRepository.findByAlbum_IdAndReturnedIsNull(ALBUM2_ID)
                .orElseThrow(() -> new NotFoundException("Not found unreturned issue for album")));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void returnAlbumNotFound() throws Exception {
        perform(MockMvcRequestBuilders.patch(String.format(ALBUM_RETURN_URL, NOT_FOUND))
                .param(RETURNED, CORRECT_RETURN_DATE.toString())
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(String.format(ALBUM_RETURN_URL, NOT_FOUND)));
    }

    @Test
    void returnAlbumUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.patch(String.format(ALBUM_RETURN_URL, ALBUM1_ID))
                .param(RETURNED, CORRECT_RETURN_DATE.toString())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void returnAlbumForbidden() throws Exception {
        perform(MockMvcRequestBuilders.patch(String.format(ALBUM_RETURN_URL, ALBUM1_ID))
                .param(RETURNED, CORRECT_RETURN_DATE.toString())
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void issueAlbum() throws Exception {
        Issuance newIssuance = getNewIssuance();
        ResultActions action = perform(MockMvcRequestBuilders.post(String.format(ALBUM_ISSUE_URL, ALBUM2_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(getNewIssuanceTo()))
                .with(csrf()))
                .andExpect(status().isCreated());
        Issuance created = ISSUANCE_MATCHER.readFromJson(action);
        newIssuance.setId(created.getId());
        ISSUANCE_MATCHER.assertMatchIgnoreFields(created, newIssuance, "album", "employee.department");
        ISSUANCE_MATCHER.assertMatchIgnoreFields(issuanceRepository.getExisted(created.id()), newIssuance,
                "album", "employee");
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void issueAlreadyIssuedAlbum() throws Exception {
        IssuanceTo newIssuanceTo = getNewIssuanceTo();
        newIssuanceTo.setAlbumId(ALBUM1_ID);
        perform(MockMvcRequestBuilders.post(String.format(ALBUM_ISSUE_URL, ALBUM1_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(newIssuanceTo))
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        IllegalRequestDataException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail("Album is already issued"))
                .andExpect(problemInstance(String.format(ALBUM_ISSUE_URL, ALBUM1_ID)));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void issueAlbumWithFutureIssueDate() throws Exception {
        IssuanceTo newIssuanceTo = getNewIssuanceTo();
        newIssuanceTo.setIssued(LocalDate.now().plusDays(5));
        perform(MockMvcRequestBuilders.post(String.format(ALBUM_ISSUE_URL, ALBUM2_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(newIssuanceTo))
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        IllegalRequestDataException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail("Issue date can not be greater than today date"))
                .andExpect(problemInstance(String.format(ALBUM_ISSUE_URL, ALBUM2_ID)));
        assertThrows(NotFoundException.class, () -> issuanceRepository.findByAlbum_IdAndReturnedIsNull(ALBUM2_ID)
                .orElseThrow(() -> new NotFoundException("Not found not returned issuance")));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void issueAlbumWithIssueDateBeforeLastIssueDate() throws Exception {
        IssuanceTo newIssuanceTo = getNewIssuanceTo();
        newIssuanceTo.setIssued(ISSUE_DATE_BEFORE_LAST_ISSUE_DATE);
        perform(MockMvcRequestBuilders.post(String.format(ALBUM_ISSUE_URL, ALBUM2_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(newIssuanceTo))
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        IllegalRequestDataException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail("Issue date must be greater than last issue date"))
                .andExpect(problemInstance(String.format(ALBUM_ISSUE_URL, ALBUM2_ID)));
        assertThrows(NotFoundException.class, () -> issuanceRepository.findByAlbum_IdAndReturnedIsNull(ALBUM2_ID)
                .orElseThrow(() -> new NotFoundException("Not found not returned issuance")));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void issueAlbumIdNotConsistent() throws Exception {
        perform(MockMvcRequestBuilders.post(String.format(ALBUM_ISSUE_URL, ALBUM1_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(getNewIssuanceTo()))
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        IllegalRequestDataException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail("IssuanceTo must has albumId=" + ALBUM1_ID))
                .andExpect(problemInstance(String.format(ALBUM_ISSUE_URL, ALBUM1_ID)));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void issueAlbumNotFound() throws Exception {
        IssuanceTo newIssuanceTo = getNewIssuanceTo();
        newIssuanceTo.setAlbumId(NOT_FOUND);
        perform(MockMvcRequestBuilders.post(String.format(ALBUM_ISSUE_URL, NOT_FOUND))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(newIssuanceTo))
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(String.format(ALBUM_ISSUE_URL, NOT_FOUND)));
    }

    @Test
    void issueAlbumUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.post(String.format(ALBUM_ISSUE_URL, ALBUM2_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(getNewIssuanceTo()))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void issueAlbumForbidden() throws Exception {
        perform(MockMvcRequestBuilders.post(String.format(ALBUM_ISSUE_URL, ALBUM2_ID))
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(getNewIssuanceTo()))
                .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
