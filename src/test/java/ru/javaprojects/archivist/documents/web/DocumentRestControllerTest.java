package ru.javaprojects.archivist.documents.web;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
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
    private static final String APPLICABILITIES_URL_SLASH = APPLICABILITIES_URL + "/";
    private static final String CONTENT_URL = DOCUMENTS_URL + "/content";
    private static final String CONTENT_URL_SLASH = CONTENT_URL + "/";

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
        String notExistedDocumentDecimalNumber = "VUIA.111111.222";
        newApplicabilityTo.setDecimalNumber(notExistedDocumentDecimalNumber);
        ResultActions action = perform(MockMvcRequestBuilders.post(APPLICABILITIES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(newApplicabilityTo))
                .with(csrf()))
                .andExpect(status().isCreated());
        Document applicability = documentService.getByDecimalNumber(notExistedDocumentDecimalNumber);
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
        ApplicabilityTo newApplicabilityTo = new ApplicabilityTo(null, DOCUMENT5_ID, "VUIA.555555.001", true);
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
        perform(MockMvcRequestBuilders.get(DOCUMENTS_URL + "/" + DOCUMENT5_ID + "/applicabilities")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(APPLICABILITY_MATCHER.contentJson(applicability2, applicability1));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void getApplicabilitiesWhenDocumentNotExists() throws Exception {
        perform(MockMvcRequestBuilders.get(DOCUMENTS_URL + "/" + NOT_FOUND + "/applicabilities")
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(DOCUMENTS_URL + "/" + NOT_FOUND + "/applicabilities"));
    }

    @Test
    void getApplicabilitiesUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.patch(DOCUMENTS_URL + "/" + DOCUMENT5_ID + "/applicabilities")
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
        perform(MockMvcRequestBuilders.get(DOCUMENTS_URL + "/" + DOCUMENT1_ID + "/content/latest")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(CONTENT_MATCHER.contentJson(content3));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void getLatestContentWhenDocumentNotExists() throws Exception {
        perform(MockMvcRequestBuilders.get(DOCUMENTS_URL + "/" + NOT_FOUND + "/content/latest")
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(DOCUMENTS_URL + "/" + NOT_FOUND + "/content/latest"));
    }

    @Test
    void getLatestContentUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.patch(DOCUMENTS_URL + "/" + DOCUMENT1_ID + "/content/latest")
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));

    }

    @Test
    @WithUserDetails(USER_MAIL)
    void getAllContents() throws Exception {
        perform(MockMvcRequestBuilders.get(DOCUMENTS_URL + "/" + DOCUMENT1_ID + "/content/all")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(CONTENT_MATCHER.contentJson(content3, content2, content1));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void getAllContentsWhenDocumentNotExists() throws Exception {
        perform(MockMvcRequestBuilders.get(DOCUMENTS_URL + "/" + NOT_FOUND + "/content/all")
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(DOCUMENTS_URL + "/" + NOT_FOUND + "/content/all"));
    }

    @Test
    void getAllContentsUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.patch(DOCUMENTS_URL + "/" + DOCUMENT1_ID + "/content/all")
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
        perform(MockMvcRequestBuilders.get(CONTENT_URL_SLASH + "download")
                .with(csrf())
                .param("fileLink", content1.getFiles().get(1).getFileLink()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "inline; filename=" + content1.getFiles().get(1).getName()));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void downloadContentFileNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(CONTENT_URL_SLASH + "download")
                .with(csrf())
                .param("fileLink", "VUIA.111111.001/0/VUIA.111111.001.pdf"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        IllegalRequestDataException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail("Failed to download file: " + "VUIA.111111.001.pdf"))
                .andExpect(problemInstance(CONTENT_URL_SLASH + "download"));
    }

    @Test
    void downloadContentFileUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(CONTENT_URL_SLASH + "download")
                .with(csrf())
                .param("fileLink", content1.getFiles().get(0).getFileLink()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createContent() throws Exception {
        generateTestDataFiles();
        Files.deleteIfExists(Paths.get(contentPath, "VUIA.465521.004/3/VUIA.465521.004.txt"));
        MockMultipartFile file = new MockMultipartFile("files", "VUIA.465521.004.txt",
                MediaType.TEXT_PLAIN_VALUE, "VUIA.465521.004 content change num 3".getBytes());
        ResultActions action = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, CONTENT_URL)
                .file(file)
                .param("id", String.valueOf(DOCUMENT1_ID))
                .param("changeNumber", "3")
                .with(csrf()))
                .andExpect(status().isCreated());
        Content created = CONTENT_MATCHER.readFromJson(action);
        Content newContent = new Content(created.getId(), 3, created.getCreated(), document1,
                List.of(new ContentFile("VUIA.465521.004.txt", "VUIA.465521.004/3/VUIA.465521.004.txt")));
        CONTENT_MATCHER.assertMatch(created, newContent);
        CONTENT_MATCHER.assertMatch(contentRepository.getExisted(created.id()), newContent);
        assertTrue(Files.exists(Paths.get(contentPath, "VUIA.465521.004/3/VUIA.465521.004.txt")));
        Files.delete(Paths.get(contentPath, "VUIA.465521.004/3/VUIA.465521.004.txt"));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createContentWhenDocumentNotExists() throws Exception {
        generateTestDataFiles();
        MockMultipartFile file = new MockMultipartFile("files", "VUIA.465521.004.txt",
                MediaType.TEXT_PLAIN_VALUE, "VUIA.465521.004 content change num 3".getBytes());
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, CONTENT_URL)
                .file(file)
                .param("id", String.valueOf(NOT_FOUND))
                .param("changeNumber", "3")
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                    NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(CONTENT_URL));
        assertTrue(Files.notExists(Paths.get(contentPath, "VUIA.465521.004/3/VUIA.465521.004.txt")));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createContentInvalid() throws Exception {
        generateTestDataFiles();
        MockMultipartFile file = new MockMultipartFile("files", "VUIA.465521.004.txt",
                MediaType.TEXT_PLAIN_VALUE, "VUIA.465521.004 content change num 3".getBytes());
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, CONTENT_URL)
                .file(file)
                .param("id", String.valueOf(DOCUMENT1_ID))
                .param("changeNumber", "-1")
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        ConstraintViolationException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemInstance(CONTENT_URL));
        assertTrue(Files.notExists(Paths.get(contentPath, "VUIA.465521.004/-1/VUIA.465521.004.txt")));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createContentDuplicateChangeNumber() throws Exception {
        generateTestDataFiles();
        MockMultipartFile file = new MockMultipartFile("files", "VUIA.465521.004.txt",
                MediaType.TEXT_PLAIN_VALUE, "VUIA.465521.004 content change num 3".getBytes());
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, CONTENT_URL)
                .file(file)
                .param("id", String.valueOf(DOCUMENT1_ID))
                .param("changeNumber", "2")
                .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        DataIntegrityViolationException.class))
                .andExpect(problemTitle(HttpStatus.CONFLICT.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.CONFLICT.value()))
                .andExpect(problemDetail(DUPLICATE_CONTENT_CHANGE_NUMBER_MESSAGE))
                .andExpect(problemInstance(CONTENT_URL));
        assertTrue(Files.notExists(Paths.get(contentPath, "VUIA.465521.004/2/VUIA.465521.004.txt")));
    }

    @Test
    void createContentUnauthorized() throws Exception {
        generateTestDataFiles();
        MockMultipartFile file = new MockMultipartFile("files", "VUIA.465521.004.txt",
                MediaType.TEXT_PLAIN_VALUE, "VUIA.465521.004 content change num 3".getBytes());
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, CONTENT_URL)
                .file(file)
                .param("id", String.valueOf(DOCUMENT1_ID))
                .param("changeNumber", "3")
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertTrue(Files.notExists(Paths.get(contentPath, "VUIA.465521.004/3/VUIA.465521.004.txt")));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createContentForbidden() throws Exception {
        generateTestDataFiles();
        MockMultipartFile file = new MockMultipartFile("files", "VUIA.465521.004.txt",
                MediaType.TEXT_PLAIN_VALUE, "VUIA.465521.004 content change num 3".getBytes());
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, CONTENT_URL)
                .file(file)
                .param("id", String.valueOf(DOCUMENT1_ID))
                .param("changeNumber", "3")
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertTrue(Files.notExists(Paths.get(contentPath, "VUIA.465521.004/3/VUIA.465521.004.txt")));
    }


    private void generateTestDataFiles() throws IOException {
        try (Stream<Path> paths = Files.walk(Paths.get(contentPath, "test-data"))) {
            paths.forEach(path -> {
                try {
                    Path newPath = Paths.get(path.toString().replaceFirst("test-data", ""));
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
