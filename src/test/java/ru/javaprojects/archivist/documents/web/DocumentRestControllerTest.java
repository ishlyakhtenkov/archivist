package ru.javaprojects.archivist.documents.web;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.javaprojects.archivist.AbstractControllerTest;
import ru.javaprojects.archivist.common.error.IllegalRequestDataException;
import ru.javaprojects.archivist.common.error.NotFoundException;
import ru.javaprojects.archivist.documents.DocumentService;
import ru.javaprojects.archivist.documents.model.Applicability;
import ru.javaprojects.archivist.documents.model.Content;
import ru.javaprojects.archivist.documents.model.ContentFile;
import ru.javaprojects.archivist.documents.model.Document;
import ru.javaprojects.archivist.documents.repository.ApplicabilityRepository;
import ru.javaprojects.archivist.documents.repository.ContentRepository;
import ru.javaprojects.archivist.documents.to.ApplicabilityTo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.archivist.CommonTestData.*;
import static ru.javaprojects.archivist.common.error.Constants.*;
import static ru.javaprojects.archivist.common.util.JsonUtil.writeValue;
import static ru.javaprojects.archivist.documents.DocumentTestData.*;
import static ru.javaprojects.archivist.documents.web.DocumentUIController.DOCUMENTS_URL;
import static ru.javaprojects.archivist.users.web.LoginController.LOGIN_URL;

class DocumentRestControllerTest extends AbstractControllerTest {
    private static final String APPLICABILITIES_URL = DOCUMENTS_URL + "/applicabilities";
    private static final String DOCUMENT_APPLICABILITIES_URL = DOCUMENTS_URL + "/%d/applicabilities";
    private static final String APPLICABILITIES_URL_SLASH = APPLICABILITIES_URL + "/";
    private static final String CONTENT_URL = DOCUMENTS_URL + "/content";
    private static final String DOCUMENT_CONTENT_LATEST_URL = DOCUMENTS_URL + "/%d/content/latest";
    private static final String DOCUMENT_CONTENT_ALL_URL = DOCUMENTS_URL + "/%d/content/all";
    private static final String CONTENT_URL_SLASH = CONTENT_URL + "/";
    private static final String CONTENT_DOWNLOAD_URL = CONTENT_URL_SLASH + "download";

    @Autowired
    private ApplicabilityRepository applicabilityRepository;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private DocumentService documentService;

    @Value("${content-path.documents}")
    private String contentPath;

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createApplicability() throws Exception {
        ResultActions action = perform(MockMvcRequestBuilders.post(APPLICABILITIES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(getNewApplicabilityTo()))
                .with(csrf()))
                .andExpect(status().isCreated());
        Applicability created = APPLICABILITY_MATCHER.readFromJson(action);
        Applicability newApplicability = new Applicability(created.getId(), document3, document1, false);
        APPLICABILITY_MATCHER.assertMatch(created, newApplicability);
        APPLICABILITY_MATCHER.assertMatch(applicabilityRepository.getExisted(created.id()), newApplicability);
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createApplicabilityWhenApplicabilityNotExists() throws Exception {
        ApplicabilityTo newApplicabilityTo = getNewApplicabilityTo();
        newApplicabilityTo.setDecimalNumber(NOT_EXISTED_DECIMAL_NUMBER);
        ResultActions action = perform(MockMvcRequestBuilders.post(APPLICABILITIES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(newApplicabilityTo))
                .with(csrf()))
                .andExpect(status().isCreated());
        Document applicability = documentService.getByDecimalNumber(NOT_EXISTED_DECIMAL_NUMBER);
        Applicability created = APPLICABILITY_MATCHER.readFromJson(action);
        Applicability newApplicability = new Applicability(created.getId(), document3, applicability, false);
        APPLICABILITY_MATCHER.assertMatch(created, newApplicability);
        APPLICABILITY_MATCHER.assertMatch(applicabilityRepository.getExisted(created.id()), newApplicability);
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createApplicabilityWhenDocumentNotExists() throws Exception {
        ApplicabilityTo newApplicabilityTo = getNewApplicabilityTo();
        newApplicabilityTo.setDocumentId(NOT_FOUND);
        perform(MockMvcRequestBuilders.post(APPLICABILITIES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(newApplicabilityTo))
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(APPLICABILITIES_URL));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createApplicabilityInvalid() throws Exception {
        ApplicabilityTo newApplicabilityTo = getNewApplicabilityTo();
        newApplicabilityTo.setDecimalNumber(null);
        perform(MockMvcRequestBuilders.post(APPLICABILITIES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(newApplicabilityTo))
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        MethodArgumentNotValidException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemInstance(APPLICABILITIES_URL));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    @Transactional(propagation = Propagation.NEVER)
    void createApplicabilityDuplicate() throws Exception {
        ApplicabilityTo newApplicabilityTo = new ApplicabilityTo(null, DOCUMENT5_ID, document3.getDecimalNumber(), false);
        perform(MockMvcRequestBuilders.post(APPLICABILITIES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(newApplicabilityTo))
                .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        DataIntegrityViolationException.class))
                .andExpect(problemTitle(HttpStatus.CONFLICT.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.CONFLICT.value()))
                .andExpect(problemDetail(DUPLICATE_DOCUMENT_APPLICABILITY_MESSAGE))
                .andExpect(problemInstance(APPLICABILITIES_URL));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    @Transactional(propagation = Propagation.NEVER)
    void createApplicabilityDuplicatePrimal() throws Exception {
        ApplicabilityTo newApplicabilityTo = new ApplicabilityTo(null, DOCUMENT5_ID, NOT_EXISTED_DECIMAL_NUMBER, true);
        perform(MockMvcRequestBuilders.post(APPLICABILITIES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(newApplicabilityTo))
                .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        DataIntegrityViolationException.class))
                .andExpect(problemTitle(HttpStatus.CONFLICT.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.CONFLICT.value()))
                .andExpect(problemDetail(DUPLICATE_PRIMAL_APPLICABILITY_MESSAGE))
                .andExpect(problemInstance(APPLICABILITIES_URL));
    }

    @Test
    void createApplicabilityUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.post(APPLICABILITIES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(getNewApplicabilityTo()))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createApplicabilityForbidden() throws Exception {
        perform(MockMvcRequestBuilders.post(APPLICABILITIES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(getNewApplicabilityTo()))
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void getApplicabilities() throws Exception {
        perform(MockMvcRequestBuilders.get(String.format(DOCUMENT_APPLICABILITIES_URL, DOCUMENT5_ID))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(APPLICABILITY_MATCHER.contentJson(applicability2, applicability1));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void getApplicabilitiesWhenDocumentNotExists() throws Exception {
        perform(MockMvcRequestBuilders.get(String.format(DOCUMENT_APPLICABILITIES_URL, NOT_FOUND))
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(String.format(DOCUMENT_APPLICABILITIES_URL, NOT_FOUND)));
    }

    @Test
    void getApplicabilitiesUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.patch(String.format(DOCUMENT_APPLICABILITIES_URL, DOCUMENT5_ID))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }


    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void deleteApplicability() throws Exception {
        perform(MockMvcRequestBuilders.delete(APPLICABILITIES_URL_SLASH + DOCUMENT_5_APPLICABILITY_1_ID)
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertThrows(NotFoundException.class, () -> applicabilityRepository.getExisted(DOCUMENT_5_APPLICABILITY_1_ID));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void deleteNotFound() throws Exception {
        perform(MockMvcRequestBuilders.delete(APPLICABILITIES_URL_SLASH + NOT_FOUND)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(APPLICABILITIES_URL_SLASH + NOT_FOUND));
    }

    @Test
    void deleteUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.delete(APPLICABILITIES_URL_SLASH + DOCUMENT_5_APPLICABILITY_1_ID)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertDoesNotThrow(() -> applicabilityRepository.getExisted(DOCUMENT_5_APPLICABILITY_1_ID));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void deleteForbidden() throws Exception {
        perform(MockMvcRequestBuilders.delete(APPLICABILITIES_URL_SLASH + DOCUMENT_5_APPLICABILITY_1_ID)
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertDoesNotThrow(() -> applicabilityRepository.getExisted(DOCUMENT_5_APPLICABILITY_1_ID));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void getLatestContent() throws Exception {
        perform(MockMvcRequestBuilders.get(String.format(DOCUMENT_CONTENT_LATEST_URL, DOCUMENT1_ID))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(CONTENT_MATCHER.contentJson(content3));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void getLatestContentWhenDocumentNotExists() throws Exception {
        perform(MockMvcRequestBuilders.get(String.format(DOCUMENT_CONTENT_LATEST_URL, NOT_FOUND))
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(String.format(DOCUMENT_CONTENT_LATEST_URL, NOT_FOUND)));
    }

    @Test
    void getLatestContentUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.patch(String.format(DOCUMENT_CONTENT_LATEST_URL, DOCUMENT1_ID))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));

    }

    @Test
    @WithUserDetails(USER_MAIL)
    void getAllContents() throws Exception {
        perform(MockMvcRequestBuilders.get(String.format(DOCUMENT_CONTENT_ALL_URL, DOCUMENT1_ID))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(CONTENT_MATCHER.contentJson(content3, content2, content1));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void getAllContentsWhenDocumentNotExists() throws Exception {
        perform(MockMvcRequestBuilders.get(String.format(DOCUMENT_CONTENT_ALL_URL, NOT_FOUND))
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(String.format(DOCUMENT_CONTENT_ALL_URL, NOT_FOUND)));
    }

    @Test
    void getAllContentsUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.patch(String.format(DOCUMENT_CONTENT_ALL_URL, DOCUMENT1_ID))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void deleteContent() throws Exception {
        generateTestDataFiles();
        perform(MockMvcRequestBuilders.delete(CONTENT_URL_SLASH + DOCUMENT_1_CONTENT_1_ID)
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertThrows(NotFoundException.class, () -> contentRepository.getExisted(DOCUMENT_1_CONTENT_1_ID));
        content1.getFiles().forEach(file -> assertTrue(Files.notExists(Paths.get(contentPath, file.getFileLink()))));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void deleteContentNotFound() throws Exception {
        generateTestDataFiles();
        perform(MockMvcRequestBuilders.delete(CONTENT_URL_SLASH + NOT_FOUND)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(CONTENT_URL_SLASH + NOT_FOUND));
    }

    @Test
    void deleteContentUnauthorized() throws Exception {
        generateTestDataFiles();
        perform(MockMvcRequestBuilders.delete(CONTENT_URL_SLASH + DOCUMENT_1_CONTENT_1_ID)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertDoesNotThrow(() -> contentRepository.getExisted(DOCUMENT_1_CONTENT_1_ID));
        content1.getFiles().forEach(file -> assertTrue(Files.exists(Paths.get(contentPath, file.getFileLink()))));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void deleteContentForbidden() throws Exception {
        generateTestDataFiles();
        perform(MockMvcRequestBuilders.delete(CONTENT_URL_SLASH + DOCUMENT_1_CONTENT_1_ID)
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertDoesNotThrow(() -> contentRepository.getExisted(DOCUMENT_1_CONTENT_1_ID));
        content1.getFiles().forEach(file -> assertTrue(Files.exists(Paths.get(contentPath, file.getFileLink()))));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void downloadContentFile() throws Exception {
        perform(MockMvcRequestBuilders.get(CONTENT_DOWNLOAD_URL)
                .with(csrf())
                .param(FILE_LINK_PARAM, content1.getFiles().get(1).getFileLink()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "inline; filename=" + content1.getFiles().get(1).getName()));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void downloadContentFileNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(CONTENT_DOWNLOAD_URL)
                .with(csrf())
                .param(FILE_LINK_PARAM,  NOT_EXISTED_CONTENT_FILE_LINK))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        IllegalRequestDataException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail("Failed to download file: " + NOT_EXISTED_CONTENT_FILE))
                .andExpect(problemInstance(CONTENT_DOWNLOAD_URL));
    }

    @Test
    void downloadContentFileUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(CONTENT_DOWNLOAD_URL)
                .with(csrf())
                .param(FILE_LINK_PARAM, content1.getFiles().get(0).getFileLink()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createContent() throws Exception {
        generateTestDataFiles();
        Files.deleteIfExists(Paths.get(contentPath, String.format(NEW_CONTENT_FILE_LINK, NOT_EXISTED_CONTENT_CHANGE_NUMBER)));
        ResultActions action = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, CONTENT_URL)
                .file(CONTENT_FILE)
                .param(ID_PARAM, String.valueOf(DOCUMENT1_ID))
                .param(CHANGE_NUMBER_PARAM, NOT_EXISTED_CONTENT_CHANGE_NUMBER)
                .with(csrf()))
                .andExpect(status().isCreated());
        Content created = CONTENT_MATCHER.readFromJson(action);
        Content newContent = new Content(created.getId(), 3, created.getCreated(), document1,
                List.of(new ContentFile(NEW_CONTENT_FILE, String.format(NEW_CONTENT_FILE_LINK, NOT_EXISTED_CONTENT_CHANGE_NUMBER))));
        CONTENT_MATCHER.assertMatch(created, newContent);
        CONTENT_MATCHER.assertMatch(contentRepository.getExisted(created.id()), newContent);
        assertTrue(Files.exists(Paths.get(contentPath, String.format(NEW_CONTENT_FILE_LINK, NOT_EXISTED_CONTENT_CHANGE_NUMBER))));
        Files.delete(Paths.get(contentPath, String.format(NEW_CONTENT_FILE_LINK, NOT_EXISTED_CONTENT_CHANGE_NUMBER)));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createContentWhenDocumentNotExists() throws Exception {
        generateTestDataFiles();
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, CONTENT_URL)
                .file(CONTENT_FILE)
                .param(ID_PARAM, String.valueOf(NOT_FOUND))
                .param(CHANGE_NUMBER_PARAM, NOT_EXISTED_CONTENT_CHANGE_NUMBER)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                    NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(CONTENT_URL));
        assertTrue(Files.notExists(Paths.get(contentPath, String.format(NEW_CONTENT_FILE_LINK, NOT_EXISTED_CONTENT_CHANGE_NUMBER))));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createContentInvalid() throws Exception {
        generateTestDataFiles();
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, CONTENT_URL)
                .file(CONTENT_FILE)
                .param(ID_PARAM, String.valueOf(DOCUMENT1_ID))
                .param(CHANGE_NUMBER_PARAM, "-1")
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        ConstraintViolationException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemInstance(CONTENT_URL));
        assertTrue(Files.notExists(Paths.get(contentPath, String.format(NEW_CONTENT_FILE_LINK, -1))));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createContentDuplicateChangeNumber() throws Exception {
        generateTestDataFiles();
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, CONTENT_URL)
                .file(CONTENT_FILE)
                .param(ID_PARAM, String.valueOf(DOCUMENT1_ID))
                .param(CHANGE_NUMBER_PARAM, String.valueOf(content3.getChangeNumber()))
                .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        DataIntegrityViolationException.class))
                .andExpect(problemTitle(HttpStatus.CONFLICT.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.CONFLICT.value()))
                .andExpect(problemDetail(DUPLICATE_CONTENT_CHANGE_NUMBER_MESSAGE))
                .andExpect(problemInstance(CONTENT_URL));
        assertTrue(Files.notExists(Paths.get(contentPath, String.format(NEW_CONTENT_FILE_LINK, content3.getChangeNumber()))));
    }

    @Test
    void createContentUnauthorized() throws Exception {
        generateTestDataFiles();
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, CONTENT_URL)
                .file(CONTENT_FILE)
                .param(ID_PARAM, String.valueOf(DOCUMENT1_ID))
                .param(CHANGE_NUMBER_PARAM, NOT_EXISTED_CONTENT_CHANGE_NUMBER)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertTrue(Files.notExists(Paths.get(contentPath, String.format(NEW_CONTENT_FILE_LINK, NOT_EXISTED_CONTENT_CHANGE_NUMBER))));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createContentForbidden() throws Exception {
        generateTestDataFiles();
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, CONTENT_URL)
                .file(CONTENT_FILE)
                .param(ID_PARAM, String.valueOf(DOCUMENT1_ID))
                .param(CHANGE_NUMBER_PARAM, NOT_EXISTED_CONTENT_CHANGE_NUMBER)
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertTrue(Files.notExists(Paths.get(contentPath, String.format(NEW_CONTENT_FILE_LINK, NOT_EXISTED_CONTENT_CHANGE_NUMBER))));
    }


    private void generateTestDataFiles() throws IOException {
        try (Stream<Path> paths = Files.walk(Paths.get(contentPath, CONTENT_TEST_DATA_DIR_NAME))) {
            paths.forEach(path -> {
                try {
                    Path newPath = Paths.get(path.toString().replaceFirst(CONTENT_TEST_DATA_DIR_NAME, ""));
                    if (Files.notExists(newPath)) {
                        Files.copy(path,newPath);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }
}
