package ru.javaprojects.archivist.changenotices.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaprojects.archivist.AbstractControllerTest;
import ru.javaprojects.archivist.ManagesContentFiles;
import ru.javaprojects.archivist.changenotices.ChangeNoticeService;
import ru.javaprojects.archivist.common.error.NotFoundException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static ru.javaprojects.archivist.CommonTestData.*;
import static ru.javaprojects.archivist.changenotices.ChangeNoticeTestData.*;
import static ru.javaprojects.archivist.changenotices.web.ChangeNoticeUIControllerTest.CHANGE_NOTICES_URL_SLASH;
import static ru.javaprojects.archivist.common.web.PathUIController.LOGIN_URL;
import static ru.javaprojects.archivist.documents.DocumentTestData.CHANGE_MATCHER;

class ChangeNoticeRestControllerTest extends AbstractControllerTest implements ManagesContentFiles {
    private static final String CHANGE_NOTICES_AUTOGENERATED_CHANGES_URL =
            CHANGE_NOTICES_URL_SLASH + "%s/changes/autogenerated";

    @Autowired
    private ChangeNoticeService changeNoticeService;

    @Value("${content-path.change-notices}")
    private String contentPath;

    @Override
    public String getContentPath() {
        return contentPath;
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void delete() throws Exception {
        perform(MockMvcRequestBuilders.delete(CHANGE_NOTICES_URL_SLASH + CHANGE_NOTICE_1_ID)
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertThrows(NotFoundException.class, () -> changeNoticeService.get(CHANGE_NOTICE_1_ID));
        assertTrue(Files.notExists(Paths.get(contentPath, changeNotice1.getName())));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void deleteNotFound() throws Exception {
        perform(MockMvcRequestBuilders.delete(CHANGE_NOTICES_URL_SLASH + NOT_FOUND)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(CHANGE_NOTICES_URL_SLASH + NOT_FOUND));
    }

    @Test
    void deleteUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.delete(CHANGE_NOTICES_URL_SLASH + CHANGE_NOTICE_1_ID)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertDoesNotThrow(() -> changeNoticeService.get(CHANGE_NOTICE_1_ID));
        assertTrue(Files.exists(Paths.get(contentPath, changeNotice1.getName())));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void deleteForbidden() throws Exception {
        perform(MockMvcRequestBuilders.delete(CHANGE_NOTICES_URL_SLASH + CHANGE_NOTICE_1_ID)
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertDoesNotThrow(() -> changeNoticeService.get(CHANGE_NOTICE_1_ID));
        assertTrue(Files.exists(Paths.get(contentPath, changeNotice1.getName())));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void getChangesForAutogenerated() throws Exception {
        perform(MockMvcRequestBuilders.get(String.format(CHANGE_NOTICES_AUTOGENERATED_CHANGES_URL, changeNotice3.getName()))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(CHANGE_MATCHER.contentJsonIgnoreFields(List.of(autogeneratedChange), "changeNotice",
                        "document.developer", "document.originalHolder"));
    }

    @Test
    void getChangesForAutogeneratedUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(String.format(CHANGE_NOTICES_AUTOGENERATED_CHANGES_URL, changeNotice3.getName()))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }
}
