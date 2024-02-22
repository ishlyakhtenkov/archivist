package ru.javaprojects.archivist.documents.web;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.MultiValueMap;
import ru.javaprojects.archivist.AbstractControllerTest;
import ru.javaprojects.archivist.TestContentManager;
import ru.javaprojects.archivist.common.error.NotFoundException;
import ru.javaprojects.archivist.documents.DocumentService;
import ru.javaprojects.archivist.documents.DocumentTestData;
import ru.javaprojects.archivist.documents.model.Content;
import ru.javaprojects.archivist.documents.model.Document;
import ru.javaprojects.archivist.documents.repository.ContentRepository;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.archivist.AbstractControllerTest.ExceptionResultMatchers.exception;
import static ru.javaprojects.archivist.CommonTestData.*;
import static ru.javaprojects.archivist.common.error.Constants.DUPLICATE_ERROR_CODE;
import static ru.javaprojects.archivist.common.web.PathUIController.LOGIN_URL;
import static ru.javaprojects.archivist.companies.CompanyTestData.COMPANY_MATCHER;
import static ru.javaprojects.archivist.companies.CompanyTestData.company3;
import static ru.javaprojects.archivist.departments.DepartmentTestData.DEPARTMENT_MATCHER;
import static ru.javaprojects.archivist.departments.DepartmentTestData.department1;
import static ru.javaprojects.archivist.documents.DocumentTestData.*;
import static ru.javaprojects.archivist.documents.web.DocumentUIController.DOCUMENTS_URL;

class DocumentUIControllerTest extends AbstractControllerTest implements TestContentManager {
    private static final String DOCUMENTS_ADD_FORM_URL = DOCUMENTS_URL + "/add";
    private static final String DOCUMENTS_EDIT_FORM_URL = DOCUMENTS_URL + "/edit/";
    static final String DOCUMENTS_URL_SLASH = DOCUMENTS_URL + "/";
    private static final String DOCUMENTS_VIEW = "documents/documents";
    private static final String DOCUMENT_VIEW = "documents/document";
    private static final String DOCUMENTS_FORM_VIEW = "documents/document-form";

    @Autowired
    private DocumentService service;

    @Autowired
    private ContentRepository contentRepository;

    @Value("${content-path.documents}")
    private String contentPath;

    @Override
    public String getContentPath() {
        return contentPath;
    }

    @Test
    @WithUserDetails(USER_MAIL)
    @SuppressWarnings("unchecked")
    void getAll() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(DOCUMENTS_URL)
                .params(getPageableParams()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(DOCUMENTS_ATTRIBUTE))
                .andExpect(view().name(DOCUMENTS_VIEW));
        Page<Document> documents = (Page<Document>) Objects.requireNonNull(actions.andReturn().getModelAndView())
                .getModel().get(DOCUMENTS_ATTRIBUTE);
        assertEquals(5, documents.getTotalElements());
        assertEquals(3, documents.getTotalPages());
        DOCUMENT_MATCHER.assertMatch(documents.getContent(), List.of(document3, document1));
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    @SuppressWarnings("unchecked")
    void getAllByKeyword() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(DOCUMENTS_URL)
                .param(KEYWORD, document2.getDecimalNumber().toLowerCase()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(DOCUMENTS_ATTRIBUTE))
                .andExpect(view().name(DOCUMENTS_VIEW));
        Page<Document> documents = (Page<Document>) Objects.requireNonNull(actions.andReturn().getModelAndView())
                .getModel().get(DOCUMENTS_ATTRIBUTE);
        assertEquals(1, documents.getTotalElements());
        assertEquals(1, documents.getTotalPages());
        DOCUMENT_MATCHER.assertMatch(documents.getContent(), List.of(document2));
    }

    @Test
    void getAllUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(DOCUMENTS_URL)
                .params(getPageableParams()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void get() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(DOCUMENTS_URL_SLASH + DOCUMENT1_ID))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(DOCUMENT_ATTRIBUTE))
                .andExpect(view().name(DOCUMENT_VIEW));
        Document document = (Document) Objects.requireNonNull(actions.andReturn()
                .getModelAndView()).getModel().get(DOCUMENT_ATTRIBUTE);
        DOCUMENT_MATCHER.assertMatch(document, document1);
        COMPANY_MATCHER.assertMatchIgnoreFields(document.getOriginalHolder(), company3, "contactPersons");
        DEPARTMENT_MATCHER.assertMatchIgnoreFields(document.getDeveloper(), department1, "employees", "boss");
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void getAutoGenerated() throws Exception {
        perform(MockMvcRequestBuilders.get(DOCUMENTS_URL_SLASH + DOCUMENT6_ID))
                .andExpect(exception().exceptionPage("Entity with id=" + DOCUMENT6_ID + " not found", NotFoundException.class));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void getNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(DOCUMENTS_URL_SLASH + NOT_FOUND))
                .andExpect(exception().exceptionPage(ENTITY_NOT_FOUND, NotFoundException.class));
    }

    @Test
    void getUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(DOCUMENTS_URL_SLASH + DOCUMENT1_ID))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void showAddForm() throws Exception {
        perform(MockMvcRequestBuilders.get(DOCUMENTS_ADD_FORM_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(DOCUMENT_ATTRIBUTE))
                .andExpect(view().name(DOCUMENTS_FORM_VIEW))
                .andExpect(result -> DOCUMENT_MATCHER.assertMatch((Document) Objects.requireNonNull(result.getModelAndView())
                        .getModel().get(DOCUMENT_ATTRIBUTE), new Document()));
    }

    @Test
    void showAddFormUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(DOCUMENTS_ADD_FORM_URL))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showAddFormForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(DOCUMENTS_ADD_FORM_URL))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void create() throws Exception {
        Document newDocument = DocumentTestData.getNew();
        ResultActions actions = perform(MockMvcRequestBuilders.post(DOCUMENTS_URL)
                .params((getNewParams()))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(flash().attribute(ACTION, "Document " + newDocument.getDecimalNumber() + " was created"));
        Document created = service.getByDecimalNumber(newDocument.getDecimalNumber());
        newDocument.setId(created.id());
        DOCUMENT_MATCHER.assertMatch(created, newDocument);
        actions.andExpect(redirectedUrl(DOCUMENTS_URL_SLASH + created.getId()));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createWhenAutogeneratedExist() throws Exception {
        Document newDocument = DocumentTestData.getNew();
        newDocument.setDecimalNumber(AUTO_GENERATED_DECIMAL_NUMBER);
        MultiValueMap<String, String> newParams = getNewParams();
        newParams.set(DECIMAL_NUMBER, AUTO_GENERATED_DECIMAL_NUMBER);
        ResultActions actions = perform(MockMvcRequestBuilders.post(DOCUMENTS_URL)
                .params(newParams)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(flash().attribute(ACTION, "Document " + AUTO_GENERATED_DECIMAL_NUMBER + " was created"));
        Document created = service.getByDecimalNumber(AUTO_GENERATED_DECIMAL_NUMBER);
        newDocument.setId(created.id());
        DOCUMENT_MATCHER.assertMatch(created, newDocument);
        DOCUMENT_MATCHER.assertMatch(service.get(DOCUMENT6_ID), newDocument);
        actions.andExpect(redirectedUrl(DOCUMENTS_URL_SLASH + created.getId()));
    }

    @Test
    void createUnAuthorize() throws Exception {
        perform(MockMvcRequestBuilders.post(DOCUMENTS_URL)
                .params((DocumentTestData.getNewParams()))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertThrows(NotFoundException.class, () -> service.getByDecimalNumber(DocumentTestData.getNew().getDecimalNumber()));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createForbidden() throws Exception {
        perform(MockMvcRequestBuilders.post(DOCUMENTS_URL)
                .params((DocumentTestData.getNewParams()))
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertThrows(NotFoundException.class, () -> service.getByDecimalNumber(DocumentTestData.getNew().getDecimalNumber()));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createInvalid() throws Exception {
        MultiValueMap<String, String> newInvalidParams = DocumentTestData.getNewInvalidParams();
        perform(MockMvcRequestBuilders.post(DOCUMENTS_URL)
                .params(newInvalidParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(DOCUMENT_ATTRIBUTE, NAME, DECIMAL_NUMBER,
                        INVENTORY_NUMBER, COMMENT))
                .andExpect(view().name(DOCUMENTS_FORM_VIEW));
        assertThrows(NotFoundException.class, () -> service.getByDecimalNumber(newInvalidParams.get(DECIMAL_NUMBER).get(0)));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createDuplicateDecimalNumber() throws Exception {
        MultiValueMap<String, String> newParams = DocumentTestData.getNewParams();
        newParams.set(DECIMAL_NUMBER, document1.getDecimalNumber());
        perform(MockMvcRequestBuilders.post(DOCUMENTS_URL)
                .params(newParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(DOCUMENT_ATTRIBUTE, DECIMAL_NUMBER, DUPLICATE_ERROR_CODE))
                .andExpect(view().name(DOCUMENTS_FORM_VIEW));
        assertNotEquals(DocumentTestData.getNew().getName(), service.getByDecimalNumber(document1.getDecimalNumber()).getName());
    }

    @Test
    void checkServiceValidationWorks() {
        Document aNew = getNew();
        aNew.setName(null);
        aNew.setInventoryNumber("");
        assertThrows(ConstraintViolationException.class, () -> service.createOrUpdate(aNew));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createDuplicateInventoryNumber() throws Exception {
        MultiValueMap<String, String> newParams = DocumentTestData.getNewParams();
        newParams.set(INVENTORY_NUMBER, document1.getInventoryNumber());
        perform(MockMvcRequestBuilders.post(DOCUMENTS_URL)
                .params(newParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(DOCUMENT_ATTRIBUTE, INVENTORY_NUMBER, DUPLICATE_ERROR_CODE))
                .andExpect(view().name(DOCUMENTS_FORM_VIEW));
        assertNotEquals(DocumentTestData.getNew().getName(), service.getByDecimalNumber(document1.getDecimalNumber()).getName());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void showEditForm() throws Exception {
        perform(MockMvcRequestBuilders.get(DOCUMENTS_EDIT_FORM_URL + DOCUMENT1_ID))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(DOCUMENT_ATTRIBUTE))
                .andExpect(view().name(DOCUMENTS_FORM_VIEW))
                .andExpect(result -> DOCUMENT_MATCHER.assertMatch((Document) Objects.requireNonNull(result.getModelAndView())
                        .getModel().get(DOCUMENT_ATTRIBUTE), document1));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void showEditFormAutoGenerated() throws Exception {
        perform(MockMvcRequestBuilders.get(DOCUMENTS_EDIT_FORM_URL + DOCUMENT6_ID))
                .andExpect(exception().exceptionPage("Entity with id=" + DOCUMENT6_ID + " not found", NotFoundException.class));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void showEditFormNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(DOCUMENTS_EDIT_FORM_URL + NOT_FOUND))
                .andExpect(exception().exceptionPage(ENTITY_NOT_FOUND, NotFoundException.class));
    }

    @Test
    void showEditFormUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(DOCUMENTS_EDIT_FORM_URL + DOCUMENT1_ID))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showEditFormForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(DOCUMENTS_EDIT_FORM_URL + DOCUMENT1_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void update() throws Exception {
        Document updatedDocument = DocumentTestData.getUpdated();
        perform(MockMvcRequestBuilders.post(DOCUMENTS_URL)
                .params(DocumentTestData.getUpdatedParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(DOCUMENTS_URL_SLASH + DOCUMENT1_ID))
                .andExpect(flash().attribute(ACTION, "Document " + updatedDocument.getDecimalNumber() + " was updated"));
        DOCUMENT_MATCHER.assertMatch(service.get(DOCUMENT1_ID), updatedDocument);

        //Check ContentFiles from all Contents have updated fileLinks
        List<Content> contents = contentRepository.findByDocument_IdOrderByChangeNumberDesc(DOCUMENT1_ID);
        contents.stream()
                .flatMap(content -> content.getFiles().stream())
                .forEach(contentFile -> assertTrue(contentFile.getFileLink()
                        .startsWith(updatedDocument.getDecimalNumber() + "/")));

        //Check all files with updated fileLinks exist
        contents.stream()
                .flatMap(content -> content.getFiles().stream())
                .forEach(contentFile -> assertTrue(Files.exists(Paths.get(contentPath, contentFile.getFileLink()))));

        //Check all files with old fileLinks not exist
        content1.getFiles().forEach(contentFile -> assertTrue(Files.notExists(Paths.get(contentPath, contentFile.getFileLink()))));
        content2.getFiles().forEach(contentFile -> assertTrue(Files.notExists(Paths.get(contentPath, contentFile.getFileLink()))));
        content3.getFiles().forEach(contentFile -> assertTrue(Files.notExists(Paths.get(contentPath, contentFile.getFileLink()))));

        //Check dir with old decimal number name not exists
        assertTrue(Files.notExists(Paths.get(contentPath, document1.getDecimalNumber())));
    }

    //Check UniqueDecimalNumberValidator works correct when update
    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void updateDecimalNumberNotChange() throws Exception {
        Document updatedDocument = DocumentTestData.getUpdated();
        updatedDocument.setDecimalNumber(document1.getDecimalNumber());
        MultiValueMap<String, String> updatedParams = DocumentTestData.getUpdatedParams();
        updatedParams.set(DECIMAL_NUMBER, document1.getDecimalNumber());
        perform(MockMvcRequestBuilders.post(DOCUMENTS_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(DOCUMENTS_URL_SLASH + DOCUMENT1_ID))
                .andExpect(flash().attribute(ACTION, "Document " + updatedDocument.getDecimalNumber() + " was updated"));
        DOCUMENT_MATCHER.assertMatch(service.get(DOCUMENT1_ID), updatedDocument);
    }

    //Check UniqueInventoryNumberValidator works correct when update
    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void updateInventoryNumberNotChange() throws Exception {
        Document updatedDocument = DocumentTestData.getUpdated();
        updatedDocument.setInventoryNumber(document1.getInventoryNumber());
        MultiValueMap<String, String> updatedParams = DocumentTestData.getUpdatedParams();
        updatedParams.set(INVENTORY_NUMBER, document1.getInventoryNumber());
        perform(MockMvcRequestBuilders.post(DOCUMENTS_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(DOCUMENTS_URL_SLASH + DOCUMENT1_ID))
                .andExpect(flash().attribute(ACTION, "Document " + updatedDocument.getDecimalNumber() + " was updated"));
        DOCUMENT_MATCHER.assertMatch(service.get(DOCUMENT1_ID), updatedDocument);
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void updateNotFound() throws Exception {
        MultiValueMap<String, String> updatedParams = DocumentTestData.getUpdatedParams();
        updatedParams.set(ID, NOT_FOUND + "");
        perform(MockMvcRequestBuilders.post(DOCUMENTS_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(exception().exceptionPage(ENTITY_NOT_FOUND, NotFoundException.class));
    }

    @Test
    void updateUnAuthorize() throws Exception {
        perform(MockMvcRequestBuilders.post(DOCUMENTS_URL)
                .params(DocumentTestData.getUpdatedParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertNotEquals(service.get(DOCUMENT1_ID).getName(), DocumentTestData.getUpdated().getName());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateForbidden() throws Exception {
        perform(MockMvcRequestBuilders.post(DOCUMENTS_URL)
                .params(DocumentTestData.getUpdatedParams())
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertNotEquals(service.get(DOCUMENT1_ID).getName(), DocumentTestData.getUpdated().getName());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void updateInvalid() throws Exception {
        MultiValueMap<String, String> updatedInvalidParams = DocumentTestData.getUpdatedInvalidParams();
        perform(MockMvcRequestBuilders.post(DOCUMENTS_URL)
                .params(updatedInvalidParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(DOCUMENT_ATTRIBUTE, NAME,
                        DECIMAL_NUMBER, INVENTORY_NUMBER, COMMENT))
                .andExpect(view().name(DOCUMENTS_FORM_VIEW));
        assertNotEquals(service.get(DOCUMENT1_ID).getName(), updatedInvalidParams.get(NAME).get(0));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void updateDuplicateDecimalNumber() throws Exception {
        MultiValueMap<String, String> updatedParams = DocumentTestData.getUpdatedParams();
        updatedParams.set(DECIMAL_NUMBER, document3.getDecimalNumber());
        perform(MockMvcRequestBuilders.post(DOCUMENTS_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(DOCUMENT_ATTRIBUTE, DECIMAL_NUMBER, DUPLICATE_ERROR_CODE))
                .andExpect(view().name(DOCUMENTS_FORM_VIEW));
        assertNotEquals(service.get(DOCUMENT1_ID).getDecimalNumber(), document3.getDecimalNumber());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void updateDuplicateInventoryNumber() throws Exception {
        MultiValueMap<String, String> updatedParams = DocumentTestData.getUpdatedParams();
        updatedParams.set(INVENTORY_NUMBER, document3.getInventoryNumber());
        perform(MockMvcRequestBuilders.post(DOCUMENTS_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(DOCUMENT_ATTRIBUTE, INVENTORY_NUMBER, DUPLICATE_ERROR_CODE))
                .andExpect(view().name(DOCUMENTS_FORM_VIEW));
        assertNotEquals(service.get(DOCUMENT1_ID).getInventoryNumber(), document3.getInventoryNumber());
    }
}
