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
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.javaprojects.archivist.AbstractControllerTest;
import ru.javaprojects.archivist.CommonTestData;
import ru.javaprojects.archivist.TestContentManager;
import ru.javaprojects.archivist.changenotices.ChangeNoticeService;
import ru.javaprojects.archivist.changenotices.model.Change;
import ru.javaprojects.archivist.changenotices.model.ChangeNotice;
import ru.javaprojects.archivist.common.error.IllegalRequestDataException;
import ru.javaprojects.archivist.common.error.NotFoundException;
import ru.javaprojects.archivist.documents.DocumentService;
import ru.javaprojects.archivist.documents.model.*;
import ru.javaprojects.archivist.documents.repository.ApplicabilityRepository;
import ru.javaprojects.archivist.documents.repository.ContentRepository;
import ru.javaprojects.archivist.documents.repository.SendingRepository;
import ru.javaprojects.archivist.documents.repository.SubscriberRepository;
import ru.javaprojects.archivist.documents.to.ApplicabilityTo;
import ru.javaprojects.archivist.documents.to.ChangeTo;
import ru.javaprojects.archivist.documents.to.SendingTo;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.archivist.CommonTestData.*;
import static ru.javaprojects.archivist.changenotices.ChangeNoticeTestData.changeNotice1;
import static ru.javaprojects.archivist.changenotices.ChangeNoticeTestData.changeNotice2;
import static ru.javaprojects.archivist.common.error.Constants.*;
import static ru.javaprojects.archivist.common.util.JsonUtil.writeValue;
import static ru.javaprojects.archivist.common.web.PathUIController.LOGIN_URL;
import static ru.javaprojects.archivist.companies.CompanyTestData.*;
import static ru.javaprojects.archivist.documents.DocumentTestData.*;
import static ru.javaprojects.archivist.documents.web.DocumentUIController.DOCUMENTS_URL;
import static ru.javaprojects.archivist.documents.web.DocumentUIControllerTest.DOCUMENTS_URL_SLASH;

class DocumentRestControllerTest extends AbstractControllerTest implements TestContentManager {
    private static final String APPLICABILITIES_URL = DOCUMENTS_URL + "/applicabilities";
    private static final String DOCUMENT_APPLICABILITIES_URL = DOCUMENTS_URL + "/%d/applicabilities";
    private static final String APPLICABILITIES_URL_SLASH = APPLICABILITIES_URL + "/";
    private static final String CONTENT_URL = DOCUMENTS_URL + "/content";
    private static final String DOCUMENT_CONTENT_LATEST_URL = DOCUMENTS_URL + "/%d/content/latest";
    private static final String DOCUMENT_CONTENT_ALL_URL = DOCUMENTS_URL + "/%d/content/all";
    private static final String CONTENT_URL_SLASH = CONTENT_URL + "/";
    private static final String CONTENT_DOWNLOAD_URL = CONTENT_URL_SLASH + "download";
    private static final String DOCUMENT_SUBSCRIBERS_URL = DOCUMENTS_URL + "/%d/subscribers";
    private static final String RESUBSCRIBE_URL = DOCUMENTS_URL + "/subscribers/%d/resubscribe";
    private static final String UNSUBSCRIBE_URL = DOCUMENTS_URL + "/subscribers/%d/unsubscribe";
    private static final String DOCUMENT_SENDINGS_BY_COMPANY_URL = DOCUMENTS_URL + "/%d/sendings/by-company";
    private static final String SENDINGS_URL = DOCUMENTS_URL + "/sendings";
    private static final String SENDINGS_URL_SLASH = SENDINGS_URL + "/";
    private static final String CHANGES_URL = DOCUMENTS_URL + "/changes";
    private static final String DOCUMENT_CHANGES_URL = DOCUMENTS_URL + "/%d/changes";
    private static final String CHANGES_URL_SLASH = CHANGES_URL + "/";

    @Autowired
    private ApplicabilityRepository applicabilityRepository;

    @Autowired
    private ContentRepository contentRepository;

    @Autowired
    private SendingRepository sendingRepository;

    @Autowired
    private SubscriberRepository subscriberRepository;

    @Autowired
    private ChangeNoticeService changeNoticeService;

    @Autowired
    private DocumentService documentService;

    @Value("${content-path.documents}")
    private String contentPath;

    @Override
    public String getContentPath() {
        return contentPath;
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void deleteDocument() throws Exception {
        perform(MockMvcRequestBuilders.delete(DOCUMENTS_URL_SLASH + DOCUMENT1_ID)
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertThrows(NotFoundException.class, () -> documentService.getWithOriginalHolderAndDeveloper(DOCUMENT1_ID));
        assertTrue(Files.notExists(Paths.get(contentPath, document1.getDecimalNumber())));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void deleteDocumentNotFound() throws Exception {
        perform(MockMvcRequestBuilders.delete(DOCUMENTS_URL_SLASH + NOT_FOUND)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(DOCUMENTS_URL_SLASH + NOT_FOUND));
    }

    @Test
    void deleteDocumentUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.delete(DOCUMENTS_URL_SLASH + DOCUMENT1_ID)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertDoesNotThrow(() -> documentService.getWithOriginalHolderAndDeveloper(DOCUMENT1_ID));
        assertTrue(Files.exists(Paths.get(contentPath, document1.getDecimalNumber())));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void deleteDocumentForbidden() throws Exception {
        perform(MockMvcRequestBuilders.delete(DOCUMENTS_URL_SLASH + DOCUMENT1_ID)
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertDoesNotThrow(() -> documentService.getWithOriginalHolderAndDeveloper(DOCUMENT1_ID));
        assertTrue(Files.exists(Paths.get(contentPath, document1.getDecimalNumber())));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createApplicability() throws Exception {
        Applicability newApplicability = getNewApplicability();
        ResultActions action = perform(MockMvcRequestBuilders.post(APPLICABILITIES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(getNewApplicabilityTo()))
                .with(csrf()))
                .andExpect(status().isCreated());
        Applicability created = APPLICABILITY_MATCHER.readFromJson(action);
        newApplicability.setId(created.getId());
        APPLICABILITY_MATCHER.assertMatch(created, newApplicability);
        APPLICABILITY_MATCHER.assertMatch(applicabilityRepository.getExisted(created.id()), newApplicability);
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createApplicabilityWhenApplicabilityNotExists() throws Exception {
        Applicability newApplicability = getNewApplicability();
        ApplicabilityTo newApplicabilityTo = getNewApplicabilityTo();
        newApplicabilityTo.setDecimalNumber(NOT_EXISTING_DECIMAL_NUMBER);
        ResultActions action = perform(MockMvcRequestBuilders.post(APPLICABILITIES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(newApplicabilityTo))
                .with(csrf()))
                .andExpect(status().isCreated());
        Applicability created = APPLICABILITY_MATCHER.readFromJson(action);
        newApplicability.setId(created.getId());
        Document autoGeneratedDocument = documentService.getByDecimalNumber(NOT_EXISTING_DECIMAL_NUMBER);
        newApplicability.setApplicability(autoGeneratedDocument);
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
        newApplicabilityTo.setDecimalNumber("");
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
    void createApplicabilityDuplicatePrimal() throws Exception {
        ApplicabilityTo newApplicabilityTo = new ApplicabilityTo(null, DOCUMENT5_ID, NOT_EXISTING_DECIMAL_NUMBER, true);
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
        perform(MockMvcRequestBuilders.get(String.format(DOCUMENT_APPLICABILITIES_URL, DOCUMENT5_ID))
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
    void deleteApplicabilityNotFound() throws Exception {
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
    void deleteApplicabilityUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.delete(APPLICABILITIES_URL_SLASH + DOCUMENT_5_APPLICABILITY_1_ID)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertDoesNotThrow(() -> applicabilityRepository.getExisted(DOCUMENT_5_APPLICABILITY_1_ID));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void deleteApplicabilityForbidden() throws Exception {
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
        perform(MockMvcRequestBuilders.get(String.format(DOCUMENT_CONTENT_LATEST_URL, DOCUMENT1_ID))
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
        perform(MockMvcRequestBuilders.get(String.format(DOCUMENT_CONTENT_ALL_URL, DOCUMENT1_ID))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void deleteContent() throws Exception {
        perform(MockMvcRequestBuilders.delete(CONTENT_URL_SLASH + DOCUMENT_1_CONTENT_1_ID)
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertThrows(NotFoundException.class, () -> contentRepository.getExisted(DOCUMENT_1_CONTENT_1_ID));
        content1.getFiles().forEach(file -> assertTrue(Files.notExists(Paths.get(contentPath, file.getFileLink()))));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void deleteContentNotFound() throws Exception {
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
                .param(FILE_LINK, content1.getFiles().get(1).getFileLink()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "inline; filename=" +  content1.getFiles().get(1).getFileName()));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void downloadContentFileNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(CONTENT_DOWNLOAD_URL)
                .with(csrf())
                .param(FILE_LINK, NOT_EXISTING_CONTENT_FILE_LINK))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        IllegalRequestDataException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail("Failed to download file: " + NOT_EXISTING_CONTENT_FILE))
                .andExpect(problemInstance(CONTENT_DOWNLOAD_URL));
    }

    @Test
    void downloadContentFileUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(CONTENT_DOWNLOAD_URL)
                .with(csrf())
                .param(FILE_LINK, content1.getFiles().get(0).getFileLink()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createContent() throws Exception {
        ResultActions action = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, CONTENT_URL)
                .file(CONTENT_FILE)
                .param(CommonTestData.ID, String.valueOf(DOCUMENT1_ID))
                .param(CHANGE_NUMBER, NOT_EXISTING_CONTENT_CHANGE_NUMBER)
                .with(csrf()))
                .andExpect(status().isCreated());
        Content created = CONTENT_MATCHER.readFromJson(action);
        Content newContent = new Content(created.getId(), 3, created.getCreated(), document1,
                List.of(new ContentFile(NEW_CONTENT_FILE, String.format(NEW_CONTENT_FILE_LINK, NOT_EXISTING_CONTENT_CHANGE_NUMBER))));
        CONTENT_MATCHER.assertMatch(created, newContent);
        CONTENT_MATCHER.assertMatch(contentRepository.getExisted(created.id()), newContent);
        assertTrue(Files.exists(Paths.get(contentPath, String.format(NEW_CONTENT_FILE_LINK, NOT_EXISTING_CONTENT_CHANGE_NUMBER))));
        Files.delete(Paths.get(contentPath, String.format(NEW_CONTENT_FILE_LINK, NOT_EXISTING_CONTENT_CHANGE_NUMBER)));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createContentWhenDocumentNotExists() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, CONTENT_URL)
                .file(CONTENT_FILE)
                .param(CommonTestData.ID, String.valueOf(NOT_FOUND))
                .param(CHANGE_NUMBER, NOT_EXISTING_CONTENT_CHANGE_NUMBER)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                    NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(CONTENT_URL));
        assertTrue(Files.notExists(Paths.get(contentPath, String.format(NEW_CONTENT_FILE_LINK, NOT_EXISTING_CONTENT_CHANGE_NUMBER))));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createContentInvalid() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, CONTENT_URL)
                .file(CONTENT_FILE)
                .param(CommonTestData.ID, String.valueOf(DOCUMENT1_ID))
                .param(CHANGE_NUMBER, "-1")
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
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, CONTENT_URL)
                .file(CONTENT_FILE)
                .param(CommonTestData.ID, String.valueOf(DOCUMENT1_ID))
                .param(CHANGE_NUMBER, String.valueOf(content3.getChangeNumber()))
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
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, CONTENT_URL)
                .file(CONTENT_FILE)
                .param(CommonTestData.ID, String.valueOf(DOCUMENT1_ID))
                .param(CHANGE_NUMBER, NOT_EXISTING_CONTENT_CHANGE_NUMBER)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertTrue(Files.notExists(Paths.get(contentPath, String.format(NEW_CONTENT_FILE_LINK, NOT_EXISTING_CONTENT_CHANGE_NUMBER))));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createContentForbidden() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, CONTENT_URL)
                .file(CONTENT_FILE)
                .param(CommonTestData.ID, String.valueOf(DOCUMENT1_ID))
                .param(CHANGE_NUMBER, NOT_EXISTING_CONTENT_CHANGE_NUMBER)
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertTrue(Files.notExists(Paths.get(contentPath, String.format(NEW_CONTENT_FILE_LINK, NOT_EXISTING_CONTENT_CHANGE_NUMBER))));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void getSubscribers() throws Exception {
        perform(MockMvcRequestBuilders.get(String.format(DOCUMENT_SUBSCRIBERS_URL, DOCUMENT1_ID))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(SUBSCRIBER_MATCHER.contentJson(subscriber2, subscriber3, subscriber1));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void getSubscribersWhenDocumentNotExists() throws Exception {
        perform(MockMvcRequestBuilders.get(String.format(DOCUMENT_SUBSCRIBERS_URL, NOT_FOUND))
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(String.format(DOCUMENT_SUBSCRIBERS_URL, NOT_FOUND)));
    }

    @Test
    void getSubscribersUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(String.format(DOCUMENT_SUBSCRIBERS_URL, DOCUMENT1_ID))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void getSendings() throws Exception {
        perform(MockMvcRequestBuilders.get(String.format(DOCUMENT_SENDINGS_BY_COMPANY_URL, DOCUMENT1_ID))
                .param(COMPANY_ID, String.valueOf(COMPANY1_ID))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(SENDING_MATCHER.contentJson(sending2, sending1));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void getSendingsWhenDocumentNotExists() throws Exception {
        perform(MockMvcRequestBuilders.get(String.format(DOCUMENT_SENDINGS_BY_COMPANY_URL, NOT_FOUND))
                .param(COMPANY_ID, String.valueOf(COMPANY1_ID))
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(String.format(DOCUMENT_SENDINGS_BY_COMPANY_URL, NOT_FOUND)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void getSendingsWhenCompanyNotExists() throws Exception {
        perform(MockMvcRequestBuilders.get(String.format(DOCUMENT_SENDINGS_BY_COMPANY_URL, DOCUMENT1_ID))
                .param(COMPANY_ID, String.valueOf(NOT_FOUND))
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(String.format(DOCUMENT_SENDINGS_BY_COMPANY_URL, DOCUMENT1_ID)));
    }

    @Test
    void getSendingsUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(String.format(DOCUMENT_SENDINGS_BY_COMPANY_URL, DOCUMENT1_ID))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createSending() throws Exception {
        Sending newSending = getNewSending();
        ResultActions action = perform(MockMvcRequestBuilders.post(SENDINGS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(getNewSendingTo()))
                .with(csrf()))
                .andExpect(status().isCreated());
        Sending created = SENDING_MATCHER.readFromJson(action);
        assertSendingMatch(created, newSending);
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createSendingWithResubscribe() throws Exception {
        SendingTo newSendingTo = getNewSendingTo();
        newSendingTo.setCompanyId(COMPANY3_ID);
        Sending newSending = getNewSending();
        newSending.getInvoice().getLetter().setCompany(company3);
        ResultActions action = perform(MockMvcRequestBuilders.post(SENDINGS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(newSendingTo))
                .with(csrf()))
                .andExpect(status().isCreated());
        Sending created = SENDING_MATCHER.readFromJson(action);
        assertSendingMatch(created, newSending);
        assertTrue(subscriberRepository.findByDocument_IdAndCompany_Id(newSendingTo.getDocumentId(), COMPANY3_ID)
                .orElseThrow().isSubscribed());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createSendingWithoutResubscribe() throws Exception {
        SendingTo newSendingTo = getNewSendingTo();
        newSendingTo.setCompanyId(COMPANY3_ID);
        newSendingTo.setInvoiceDate(LocalDate.of(2020, Month.DECEMBER, 1));
        Sending newSending = getNewSending();
        newSending.getInvoice().getLetter().setCompany(company3);
        newSending.getInvoice().setDate(LocalDate.of(2020, Month.DECEMBER, 1));
        ResultActions action = perform(MockMvcRequestBuilders.post(SENDINGS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(newSendingTo))
                .with(csrf()))
                .andExpect(status().isCreated());
        Sending created = SENDING_MATCHER.readFromJson(action);
        assertSendingMatch(created, newSending);
        assertFalse(subscriberRepository.findByDocument_IdAndCompany_Id(newSendingTo.getDocumentId(), COMPANY3_ID)
                .orElseThrow().isSubscribed());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createSendingWhenSubscriberCreates() throws Exception {
        SendingTo newSendingTo = getNewSendingTo();
        newSendingTo.setDocumentId(DOCUMENT3_ID);
        Sending newSending = getNewSending();
        newSending.setDocument(document3);
        ResultActions action = perform(MockMvcRequestBuilders.post(SENDINGS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(newSendingTo))
                .with(csrf()))
                .andExpect(status().isCreated());
        Sending created = SENDING_MATCHER.readFromJson(action);
        assertSendingMatch(created, newSending);
        Subscriber createdSubscriber = subscriberRepository
                .findByDocument_IdAndCompany_Id(DOCUMENT3_ID, newSendingTo.getCompanyId()).orElseThrow();
        SUBSCRIBER_MATCHER.assertMatch(createdSubscriber,
                new Subscriber(createdSubscriber.getId(), document3, company1, true, newSendingTo.getStatus()));
    }

    private void assertSendingMatch(Sending actual, Sending expected) {
        expected.setId(actual.getId());
        expected.getInvoice().setId(actual.getInvoice().getId());
        expected.getInvoice().getLetter().setId(actual.getInvoice().getLetter().getId());
        SENDING_MATCHER.assertMatch(actual, expected);
        SENDING_MATCHER.assertMatch(sendingRepository.findByIdWithInvoice(actual.id()).orElseThrow(), expected);
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createSendingWhenDocumentNotExists() throws Exception {
        SendingTo newSendingTo = getNewSendingTo();
        newSendingTo.setDocumentId(NOT_FOUND);
        perform(MockMvcRequestBuilders.post(SENDINGS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(newSendingTo))
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(SENDINGS_URL));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createSendingWhenCompanyNotExists() throws Exception {
        SendingTo newSendingTo = getNewSendingTo();
        newSendingTo.setCompanyId(NOT_FOUND);
        perform(MockMvcRequestBuilders.post(SENDINGS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(newSendingTo))
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(SENDINGS_URL));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createSendingInvalid() throws Exception {
        SendingTo newSendingTo = getNewSendingTo();
        newSendingTo.setInvoiceNumber("");
        perform(MockMvcRequestBuilders.post(SENDINGS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(newSendingTo))
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        MethodArgumentNotValidException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemInstance(SENDINGS_URL));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createSendingWithOriginalStatus() throws Exception {
        SendingTo newSendingTo = getNewSendingTo();
        newSendingTo.setStatus(Status.ORIGINAL);
        perform(MockMvcRequestBuilders.post(SENDINGS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(newSendingTo))
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        MethodArgumentNotValidException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemInstance(SENDINGS_URL));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createSendingDuplicateInvoice() throws Exception {
        SendingTo newSendingTo = getNewSendingTo();
        newSendingTo.setInvoiceNumber(sending1.getInvoice().getNumber());
        newSendingTo.setInvoiceDate(sending1.getInvoice().getDate());
        newSendingTo.setStatus(sending1.getInvoice().getStatus());
        perform(MockMvcRequestBuilders.post(SENDINGS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(newSendingTo))
                .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        DataIntegrityViolationException.class))
                .andExpect(problemTitle(HttpStatus.CONFLICT.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.CONFLICT.value()))
                .andExpect(problemDetail(DUPLICATE_SENDING_INVOICE_NUMBER_MESSAGE))
                .andExpect(problemInstance(SENDINGS_URL));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createSendingDuplicateInvoiceWhenDifferentStatus() throws Exception {
        SendingTo newSendingTo = getNewSendingTo();
        newSendingTo.setInvoiceNumber(sending1.getInvoice().getNumber());
        newSendingTo.setInvoiceDate(sending1.getInvoice().getDate());
        perform(MockMvcRequestBuilders.post(SENDINGS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(newSendingTo))
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        IllegalRequestDataException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail("specified invoice exists and has " + sending1.getInvoice().getStatus() + " status"))
                .andExpect(problemInstance(SENDINGS_URL));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createSendingDuplicateInvoiceWhenDifferentCompany() throws Exception {
        SendingTo newSendingTo = getNewSendingTo();
        newSendingTo.setInvoiceNumber(sending1.getInvoice().getNumber());
        newSendingTo.setInvoiceDate(sending1.getInvoice().getDate());
        newSendingTo.setCompanyId(COMPANY2_ID);
        perform(MockMvcRequestBuilders.post(SENDINGS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(newSendingTo))
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        IllegalRequestDataException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail("specified invoice exists and addressed to " + company1.getName()))
                .andExpect(problemInstance(SENDINGS_URL));
    }

    @Test
    void createSendingUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.post(SENDINGS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(getNewSendingTo()))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createSendingForbidden() throws Exception {
        perform(MockMvcRequestBuilders.post(SENDINGS_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(getNewSendingTo()))
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void resubscribe() throws Exception {
        perform(MockMvcRequestBuilders.patch(String.format(RESUBSCRIBE_URL, DOCUMENT_1_SUBSCRIBER_3_ID))
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertTrue(subscriberRepository.getExisted(DOCUMENT_1_SUBSCRIBER_3_ID).isSubscribed());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void resubscribeNotFound() throws Exception {
        perform(MockMvcRequestBuilders.patch(String.format(RESUBSCRIBE_URL, NOT_FOUND))
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(String.format(RESUBSCRIBE_URL, NOT_FOUND)));
    }

    @Test
    void resubscribeUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.patch(String.format(RESUBSCRIBE_URL, DOCUMENT_1_SUBSCRIBER_3_ID))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void resubscribeForbidden() throws Exception {
        perform(MockMvcRequestBuilders.patch(String.format(RESUBSCRIBE_URL, DOCUMENT_1_SUBSCRIBER_3_ID))
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void unsubscribe() throws Exception {
        perform(MockMvcRequestBuilders.patch(String.format(UNSUBSCRIBE_URL, DOCUMENT_1_SUBSCRIBER_1_ID))
                .param(UNSUBSCRIBE_REASON, REASON_FOR_UNSUBSCRIBE)
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertFalse(subscriberRepository.getExisted(DOCUMENT_1_SUBSCRIBER_1_ID).isSubscribed());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void unsubscribeInvalidReason() throws Exception {
        perform(MockMvcRequestBuilders.patch(String.format(UNSUBSCRIBE_URL, DOCUMENT_1_SUBSCRIBER_1_ID))
                .param(UNSUBSCRIBE_REASON, "")
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        ConstraintViolationException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemInstance(String.format(UNSUBSCRIBE_URL, DOCUMENT_1_SUBSCRIBER_1_ID)));
        assertTrue(subscriberRepository.getExisted(DOCUMENT_1_SUBSCRIBER_1_ID).isSubscribed());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void unsubscribeNotFound() throws Exception {
        perform(MockMvcRequestBuilders.patch(String.format(UNSUBSCRIBE_URL, NOT_FOUND))
                .param(UNSUBSCRIBE_REASON, REASON_FOR_UNSUBSCRIBE)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(String.format(UNSUBSCRIBE_URL, NOT_FOUND)));
    }

    @Test
    void unsubscribeUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.patch(String.format(UNSUBSCRIBE_URL, DOCUMENT_1_SUBSCRIBER_1_ID))
                .param(UNSUBSCRIBE_REASON, REASON_FOR_UNSUBSCRIBE)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void unsubscribeForbidden() throws Exception {
        perform(MockMvcRequestBuilders.patch(String.format(UNSUBSCRIBE_URL, DOCUMENT_1_SUBSCRIBER_1_ID))
                .param(UNSUBSCRIBE_REASON, REASON_FOR_UNSUBSCRIBE)
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void deleteSendingWhenSubscriberNotDeletes() throws Exception {
        perform(MockMvcRequestBuilders.delete(SENDINGS_URL_SLASH + DOCUMENT_1_COMPANY_1_SENDING_2_ID)
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertThrows(NotFoundException.class, () -> sendingRepository.getExisted(DOCUMENT_1_SUBSCRIBER_3_ID));
        assertDoesNotThrow(() -> subscriberRepository.getExisted(DOCUMENT_1_SUBSCRIBER_3_ID));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void deleteSendingWhenSubscriberDeletes() throws Exception {
        perform(MockMvcRequestBuilders.delete(SENDINGS_URL_SLASH + DOCUMENT_1_COMPANY_1_SENDING_1_ID)
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertThrows(NotFoundException.class, () -> sendingRepository.getExisted(DOCUMENT_1_COMPANY_1_SENDING_1_ID));
        assertDoesNotThrow(() -> subscriberRepository.getExisted(DOCUMENT_1_SUBSCRIBER_1_ID));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void deleteSendingNotFound() throws Exception {
        perform(MockMvcRequestBuilders.delete(SENDINGS_URL_SLASH + NOT_FOUND)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(SENDINGS_URL_SLASH + NOT_FOUND));
    }

    @Test
    void deleteSendingUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.delete(SENDINGS_URL_SLASH + DOCUMENT_1_COMPANY_1_SENDING_1_ID)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void deleteSendingForbidden() throws Exception {
        perform(MockMvcRequestBuilders.delete(SENDINGS_URL_SLASH + DOCUMENT_1_COMPANY_1_SENDING_1_ID)
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createChange() throws Exception {
        ChangeTo newChangeTo = getNewChangeTo();
        ResultActions action = perform(MockMvcRequestBuilders.post(CHANGES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(newChangeTo))
                .with(csrf()))
                .andExpect(status().isCreated());
        Change created = CHANGE_MATCHER.readFromJson(action);
        ChangeNotice changeNotice = ChangeNotice.autoGenerate(newChangeTo.getChangeNoticeName(), newChangeTo.getChangeNoticeDate());
        changeNotice.setId(created.getChangeNotice().getId());
        Change newChange = new Change(created.getId(), document1, changeNotice, newChangeTo.getChangeNumber());
        CHANGE_MATCHER.assertMatchIgnoreFields(created, newChange, "document.developer", "document.originalHolder");
        CHANGE_MATCHER.assertMatch(changeNoticeService.getChange(created.id()), newChange);
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createChangeWhenChangeNoticeExists() throws Exception {
        ChangeTo newChangeTo = getNewChangeToWithExistedChangeNotice();
        ResultActions action = perform(MockMvcRequestBuilders.post(CHANGES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(newChangeTo))
                .with(csrf()))
                .andExpect(status().isCreated());
        Change created = CHANGE_MATCHER.readFromJson(action);
        Change newChange = new Change(created.getId(), document2, changeNotice2, newChangeTo.getChangeNumber());
        CHANGE_MATCHER.assertMatchIgnoreFields(created, newChange, "document", "changeNotice.developer");
        CHANGE_MATCHER.assertMatchIgnoreFields(changeNoticeService.getChange(created.id()), newChange,  "document", "changeNotice");
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createChangeWhenChangeNoticeExistsDifferentChangeDate() throws Exception {
        ChangeTo newChangeTo = getNewChangeToWithExistedChangeNotice();
        newChangeTo.setChangeNoticeDate(LocalDate.of(2021, Month.DECEMBER, 15));
        perform(MockMvcRequestBuilders.post(CHANGES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(newChangeTo))
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        IllegalRequestDataException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemDetail("Change notice " + newChangeTo.getChangeNoticeName() +
                        " already exists and has release date: " + LocalDate.of(2021, Month.DECEMBER, 14)))
                .andExpect(problemInstance(CHANGES_URL));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createChangeWhenDocumentNotExists() throws Exception {
        ChangeTo newChangeTo = getNewChangeTo();
        newChangeTo.setDocumentId(NOT_FOUND);
        perform(MockMvcRequestBuilders.post(CHANGES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(newChangeTo))
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(CHANGES_URL));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createChangeInvalid() throws Exception {
        ChangeTo newChangeTo = getNewChangeTo();
        newChangeTo.setChangeNoticeName("");
        perform(MockMvcRequestBuilders.post(CHANGES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(newChangeTo))
                .with(csrf()))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        MethodArgumentNotValidException.class))
                .andExpect(problemTitle(HttpStatus.UNPROCESSABLE_ENTITY.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.UNPROCESSABLE_ENTITY.value()))
                .andExpect(problemInstance(CHANGES_URL));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createChangeDuplicateChangeNotice() throws Exception {
        ChangeTo newChangeTo = getNewChangeTo();
        newChangeTo.setChangeNoticeName(changeNotice1.getName());
        newChangeTo.setChangeNoticeDate(changeNotice1.getReleaseDate());
        perform(MockMvcRequestBuilders.post(CHANGES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(newChangeTo))
                .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        DataIntegrityViolationException.class))
                .andExpect(problemTitle(HttpStatus.CONFLICT.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.CONFLICT.value()))
                .andExpect(problemDetail(DUPLICATE_DOCUMENT_CHANGE_NOTICE_MESSAGE))
                .andExpect(problemInstance(CHANGES_URL));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createChangeDuplicateChangeNumber() throws Exception {
        ChangeTo newChangeTo = getNewChangeTo();
        newChangeTo.setChangeNumber(2);
        perform(MockMvcRequestBuilders.post(CHANGES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(newChangeTo))
                .with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        DataIntegrityViolationException.class))
                .andExpect(problemTitle(HttpStatus.CONFLICT.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.CONFLICT.value()))
                .andExpect(problemDetail(DUPLICATE_DOCUMENT_CHANGE_NUMBER_MESSAGE))
                .andExpect(problemInstance(CHANGES_URL));
    }

    @Test
    void createChangeUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.post(CHANGES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(getNewChangeTo()))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createChangeForbidden() throws Exception {
        perform(MockMvcRequestBuilders.post(CHANGES_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(writeValue(getNewChangeTo()))
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void getChanges() throws Exception {
        perform(MockMvcRequestBuilders.get(String.format(DOCUMENT_CHANGES_URL, DOCUMENT1_ID))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(CHANGE_MATCHER.contentJson(change2, change1));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void getChangesWhenDocumentNotExists() throws Exception {
        perform(MockMvcRequestBuilders.get(String.format(DOCUMENT_CHANGES_URL, NOT_FOUND))
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(String.format(DOCUMENT_CHANGES_URL, NOT_FOUND)));
    }

    @Test
    void getChangesUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(String.format(DOCUMENT_CHANGES_URL, DOCUMENT1_ID))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void deleteChange() throws Exception {
        perform(MockMvcRequestBuilders.delete(CHANGES_URL_SLASH + DOCUMENT_1_CHANGE_2_ID)
                .with(csrf()))
                .andExpect(status().isNoContent());
        assertThrows(NotFoundException.class, () -> changeNoticeService.getChange(DOCUMENT_1_CHANGE_2_ID));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void deleteChangeNotFound() throws Exception {
        perform(MockMvcRequestBuilders.delete(CHANGES_URL_SLASH + NOT_FOUND)
                .with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertEquals(Objects.requireNonNull(result.getResolvedException()).getClass(),
                        NotFoundException.class))
                .andExpect(problemTitle(HttpStatus.NOT_FOUND.getReasonPhrase()))
                .andExpect(problemStatus(HttpStatus.NOT_FOUND.value()))
                .andExpect(problemDetail(ENTITY_NOT_FOUND))
                .andExpect(problemInstance(CHANGES_URL_SLASH + NOT_FOUND));
    }

    @Test
    void deleteChangeUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.delete(CHANGES_URL_SLASH + DOCUMENT_1_CHANGE_2_ID)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertDoesNotThrow(() -> changeNoticeService.getChange(DOCUMENT_1_CHANGE_2_ID));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void deleteChangeForbidden() throws Exception {
        perform(MockMvcRequestBuilders.delete(CHANGES_URL_SLASH + DOCUMENT_1_CHANGE_2_ID)
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertDoesNotThrow(() -> changeNoticeService.getChange(DOCUMENT_1_CHANGE_2_ID));
    }
}
