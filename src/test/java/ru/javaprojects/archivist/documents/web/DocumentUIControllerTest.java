package ru.javaprojects.archivist.documents.web;

import jakarta.persistence.EntityManager;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.MultiValueMap;
import ru.javaprojects.archivist.AbstractControllerTest;
import ru.javaprojects.archivist.common.error.NotFoundException;
import ru.javaprojects.archivist.documents.DocumentService;
import ru.javaprojects.archivist.documents.DocumentTestData;
import ru.javaprojects.archivist.documents.model.Document;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.archivist.AbstractControllerTest.ExceptionResultMatchers.exception;
import static ru.javaprojects.archivist.CommonTestData.*;
import static ru.javaprojects.archivist.common.error.Constants.DUPLICATE_ERROR_CODE;
import static ru.javaprojects.archivist.companies.CompanyTestData.COMPANY_MATCHER;
import static ru.javaprojects.archivist.companies.CompanyTestData.company3;
import static ru.javaprojects.archivist.departments.DepartmentTestData.DEPARTMENT_MATCHER;
import static ru.javaprojects.archivist.departments.DepartmentTestData.department1;
import static ru.javaprojects.archivist.documents.DocumentTestData.*;
import static ru.javaprojects.archivist.documents.web.DocumentUIController.DOCUMENTS_URL;
import static ru.javaprojects.archivist.users.web.LoginController.LOGIN_URL;

class DocumentUIControllerTest extends AbstractControllerTest {
    private static final String DOCUMENTS_ADD_FORM_URL = DOCUMENTS_URL + "/add";
    private static final String DOCUMENTS_EDIT_FORM_URL = DOCUMENTS_URL + "/edit/";
    private static final String DOCUMENTS_DELETE_URL = DOCUMENTS_URL + "/delete/";
    private static final String DOCUMENTS_URL_SLASH = DOCUMENTS_URL + "/";

    private static final String DOCUMENTS_VIEW = "documents/documents";
    private static final String DOCUMENTS_DETAILS_VIEW = "documents/document-details";
    private static final String DOCUMENTS_FORM_VIEW = "documents/document-form";

    @Autowired
    private DocumentService service;

    @Autowired
    private EntityManager entityManager;

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
                .param(KEYWORD, "upia.421478"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(DOCUMENTS_ATTRIBUTE))
                .andExpect(view().name(DOCUMENTS_VIEW));
        Page<Document> documents = (Page<Document>) Objects.requireNonNull(actions.andReturn().getModelAndView())
                .getModel().get(DOCUMENTS_ATTRIBUTE);
        assertEquals(1, documents.getTotalElements());
        assertEquals(1, documents.getTotalPages());
        DOCUMENT_MATCHER.assertMatch(documents.getContent(), List.of(document3));
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
    void showDocumentDetails() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(DOCUMENTS_URL_SLASH + DOCUMENT1_ID))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(DOCUMENT_ATTRIBUTE))
                .andExpect(view().name(DOCUMENTS_DETAILS_VIEW));
        Document document = (Document) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(DOCUMENT_ATTRIBUTE);
        DOCUMENT_MATCHER.assertMatch(document, document1);
        COMPANY_MATCHER.assertMatch(document.getOriginalHolder(), company3);
        DEPARTMENT_MATCHER.assertMatch(document.getDeveloper(), department1);
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void showDocumentDetailsNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(DOCUMENTS_URL_SLASH + NOT_FOUND))
                .andExpect(exception().exceptionPage(ENTITY_NOT_FOUND, NotFoundException.class));
    }

    @Test
    void showDocumentDetailsUnAuthorized() throws Exception {
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
                .andExpect(result ->
                        DOCUMENT_MATCHER.assertMatch((Document) Objects.requireNonNull(result.getModelAndView()).getModel().get(DOCUMENT_ATTRIBUTE),
                                new Document()));
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
        newDocument.setDecimalNumber("VUIA.652147.001");
        MultiValueMap<String, String> newParams = getNewParams();
        newParams.set(DECIMAL_NUMBER_PARAM, "VUIA.652147.001");
        ResultActions actions = perform(MockMvcRequestBuilders.post(DOCUMENTS_URL)
                .params(newParams)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(flash().attribute(ACTION, "Document " + newDocument.getDecimalNumber() + " was created"));
        Document created = service.getByDecimalNumber(newDocument.getDecimalNumber());
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
                .andExpect(model().attributeHasFieldErrors(DOCUMENT_ATTRIBUTE, NAME_PARAM,
                        DECIMAL_NUMBER_PARAM, INVENTORY_NUMBER_PARAM, COMMENT_PARAM))
                .andExpect(view().name(DOCUMENTS_FORM_VIEW));
        assertThrows(NotFoundException.class, () -> service.getByDecimalNumber(newInvalidParams.get(DECIMAL_NUMBER_PARAM).get(0)));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createDuplicateDecimalNumber() throws Exception {
        MultiValueMap<String, String> newParams = DocumentTestData.getNewParams();
        newParams.set(DECIMAL_NUMBER_PARAM, document1.getDecimalNumber());
        perform(MockMvcRequestBuilders.post(DOCUMENTS_URL)
                .params(newParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(DOCUMENT_ATTRIBUTE, DECIMAL_NUMBER_PARAM, DUPLICATE_ERROR_CODE))
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
        newParams.set(INVENTORY_NUMBER_PARAM, document1.getInventoryNumber());
        perform(MockMvcRequestBuilders.post(DOCUMENTS_URL)
                .params(newParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(DOCUMENT_ATTRIBUTE, INVENTORY_NUMBER_PARAM, DUPLICATE_ERROR_CODE))
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
                .andExpect(result ->
                        DOCUMENT_MATCHER.assertMatch((Document) Objects.requireNonNull(result.getModelAndView()).getModel().get(DOCUMENT_ATTRIBUTE), document1));
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
    }

    //Check UniqueDecimalNumberValidator works correct when update
    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void updateDecimalNumberNotChange() throws Exception {
        Document updatedDocument = DocumentTestData.getUpdated();
        updatedDocument.setDecimalNumber(document1.getDecimalNumber());
        MultiValueMap<String, String> updatedParams = DocumentTestData.getUpdatedParams();
        updatedParams.set(DECIMAL_NUMBER_PARAM, document1.getDecimalNumber());
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
        updatedParams.set(INVENTORY_NUMBER_PARAM, document1.getInventoryNumber());
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
                .andExpect(model().attributeHasFieldErrors(DOCUMENT_ATTRIBUTE, NAME_PARAM,
                        DECIMAL_NUMBER_PARAM, INVENTORY_NUMBER_PARAM, COMMENT_PARAM))
                .andExpect(view().name(DOCUMENTS_FORM_VIEW));
        assertNotEquals(service.get(DOCUMENT1_ID).getName(), updatedInvalidParams.get(NAME_PARAM).get(0));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void updateDuplicateDecimalNumber() throws Exception {
        MultiValueMap<String, String> updatedParams = DocumentTestData.getUpdatedParams();
        updatedParams.set(DECIMAL_NUMBER_PARAM, document3.getDecimalNumber());
        perform(MockMvcRequestBuilders.post(DOCUMENTS_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(DOCUMENT_ATTRIBUTE, DECIMAL_NUMBER_PARAM, DUPLICATE_ERROR_CODE))
                .andExpect(view().name(DOCUMENTS_FORM_VIEW));
        assertNotEquals(service.get(DOCUMENT1_ID).getDecimalNumber(), document3.getDecimalNumber());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void updateDuplicateInventoryNumber() throws Exception {
        MultiValueMap<String, String> updatedParams = DocumentTestData.getUpdatedParams();
        updatedParams.set(INVENTORY_NUMBER_PARAM, document3.getInventoryNumber());
        perform(MockMvcRequestBuilders.post(DOCUMENTS_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(DOCUMENT_ATTRIBUTE, INVENTORY_NUMBER_PARAM, DUPLICATE_ERROR_CODE))
                .andExpect(view().name(DOCUMENTS_FORM_VIEW));
        assertNotEquals(service.get(DOCUMENT1_ID).getInventoryNumber(), document3.getInventoryNumber());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void delete() throws Exception {
        perform(MockMvcRequestBuilders.post(DOCUMENTS_DELETE_URL + DOCUMENT1_ID)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(DOCUMENTS_URL))
                .andExpect(flash().attribute(ACTION, "Document " + document1.getDecimalNumber() + " was deleted"));
        entityManager.clear();
        assertThrows(NotFoundException.class, () -> service.get(DOCUMENT1_ID));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void deleteNotFound() throws Exception {
        perform(MockMvcRequestBuilders.post(DOCUMENTS_DELETE_URL + NOT_FOUND)
                .with(csrf()))
                .andExpect(exception().exceptionPage(ENTITY_NOT_FOUND, NotFoundException.class));
    }

    @Test
    void deleteUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.post(DOCUMENTS_DELETE_URL + DOCUMENT1_ID)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertDoesNotThrow(() -> service.get(DOCUMENT1_ID));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void deleteForbidden() throws Exception {
        perform(MockMvcRequestBuilders.post(DOCUMENTS_DELETE_URL + DOCUMENT1_ID)
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertDoesNotThrow(() -> service.get(DOCUMENT1_ID));
    }
}
