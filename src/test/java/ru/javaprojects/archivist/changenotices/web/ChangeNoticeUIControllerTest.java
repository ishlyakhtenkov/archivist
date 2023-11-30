package ru.javaprojects.archivist.changenotices.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.MultiValueMap;
import ru.javaprojects.archivist.AbstractControllerTest;
import ru.javaprojects.archivist.ManagesContentFiles;
import ru.javaprojects.archivist.changenotices.ChangeNoticeService;
import ru.javaprojects.archivist.changenotices.ChangeNoticeTestData;
import ru.javaprojects.archivist.changenotices.ChangeNoticeUtil;
import ru.javaprojects.archivist.changenotices.model.ChangeNotice;
import ru.javaprojects.archivist.changenotices.to.ChangeNoticeTo;
import ru.javaprojects.archivist.common.error.IllegalRequestDataException;
import ru.javaprojects.archivist.common.error.NotFoundException;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.archivist.AbstractControllerTest.ExceptionResultMatchers.exception;
import static ru.javaprojects.archivist.CommonTestData.*;
import static ru.javaprojects.archivist.changenotices.ChangeNoticeTestData.*;
import static ru.javaprojects.archivist.changenotices.web.ChangeNoticeUIController.CHANGE_NOTICES_URL;
import static ru.javaprojects.archivist.common.error.Constants.DUPLICATE_ERROR_CODE;
import static ru.javaprojects.archivist.documents.DocumentTestData.*;
import static ru.javaprojects.archivist.users.web.LoginController.LOGIN_URL;

class ChangeNoticeUIControllerTest extends AbstractControllerTest implements ManagesContentFiles {
    private static final String CHANGE_NOTICES_ADD_FORM_URL = CHANGE_NOTICES_URL + "/add";
    private static final String CHANGE_NOTICES_EDIT_FORM_URL = CHANGE_NOTICES_URL + "/edit/";
    static final String CHANGE_NOTICES_URL_SLASH = CHANGE_NOTICES_URL + "/";
    static final String CHANGE_NOTICES_FILE_DOWNLOAD_URL = CHANGE_NOTICES_URL_SLASH + "download";


    private static final String CHANGE_NOTICES_VIEW = "change-notices/change-notices";
    private static final String CHANGE_NOTICE_VIEW = "change-notices/change-notice";
    private static final String CHANGE_NOTICE_FORM_VIEW = "change-notices/change-notice-form";

    @Autowired
    private ChangeNoticeService service;

    @Autowired
    private ChangeNoticeUtil changeNoticeUtil;

    @Value("${content-path.change-notices}")
    private String contentPath;

    @Override
    public String getContentPath() {
        return contentPath;
    }

    @Test
    @WithUserDetails(USER_MAIL)
    @SuppressWarnings("unchecked")
    void getAll() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(CHANGE_NOTICES_URL)
                .params(getPageableParams()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(CHANGE_NOTICES_ATTRIBUTE))
                .andExpect(view().name(CHANGE_NOTICES_VIEW));
        Page<ChangeNotice> changeNotices = (Page<ChangeNotice>) Objects.requireNonNull(actions.andReturn().getModelAndView())
                .getModel().get(CHANGE_NOTICES_ATTRIBUTE);
        assertEquals(2, changeNotices.getTotalElements());
        assertEquals(1, changeNotices.getTotalPages());
        CHANGE_NOTICE_MATCHER.assertMatchIgnoreFields(changeNotices.getContent(), List.of(changeNotice1, changeNotice2), "developer", "changes");
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    @SuppressWarnings("unchecked")
    void getAllByKeyword() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(CHANGE_NOTICES_URL)
                .param(KEYWORD, "vuia.sk"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(CHANGE_NOTICES_ATTRIBUTE))
                .andExpect(view().name(CHANGE_NOTICES_VIEW));
        Page<ChangeNotice> changeNotices = (Page<ChangeNotice>) Objects.requireNonNull(actions.andReturn().getModelAndView())
                .getModel().get(CHANGE_NOTICES_ATTRIBUTE);
        assertEquals(1, changeNotices.getTotalElements());
        assertEquals(1, changeNotices.getTotalPages());
        CHANGE_NOTICE_MATCHER.assertMatchIgnoreFields(changeNotices.getContent(), List.of(changeNotice1), "developer", "changes");
    }

    @Test
    void getAllUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(CHANGE_NOTICES_URL)
                .params(getPageableParams()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void get() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(CHANGE_NOTICES_URL_SLASH + CHANGE_NOTICE_1_ID))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(CHANGE_NOTICE_ATTRIBUTE))
                .andExpect(view().name(CHANGE_NOTICE_VIEW));
        ChangeNotice changeNotice = (ChangeNotice) Objects.requireNonNull(actions.andReturn().getModelAndView()).getModel().get(CHANGE_NOTICE_ATTRIBUTE);
        CHANGE_NOTICE_MATCHER.assertMatchIgnoreFields(changeNotice, changeNotice1, "changes.document.originalHolder", "changes.document.developer");
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void getNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(CHANGE_NOTICES_URL_SLASH + NOT_FOUND))
                .andExpect(exception().exceptionPage(ENTITY_NOT_FOUND, NotFoundException.class));
    }

    @Test
    void getUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(CHANGE_NOTICES_URL_SLASH + CHANGE_NOTICE_1_ID))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void downloadContentFile() throws Exception {
        perform(MockMvcRequestBuilders.get(CHANGE_NOTICES_FILE_DOWNLOAD_URL)
                .with(csrf())
                .param(FILE_LINK_PARAM, changeNotice1.getFile().getFileLink()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "inline; filename=" + changeNotice1.getFile().getFileName()));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void downloadContentFileNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(CHANGE_NOTICES_FILE_DOWNLOAD_URL)
                .with(csrf())
                .param(FILE_LINK_PARAM,  NOT_EXISTED_CONTENT_FILE_LINK))
                .andExpect(exception().exceptionPage("Failed to download file: " + NOT_EXISTED_CONTENT_FILE, IllegalRequestDataException.class));
    }

    @Test
    void downloadContentFileUnauthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(CHANGE_NOTICES_FILE_DOWNLOAD_URL)
                .with(csrf())
                .param(FILE_LINK_PARAM, changeNotice1.getFile().getFileLink()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void showAddForm() throws Exception {
        perform(MockMvcRequestBuilders.get(CHANGE_NOTICES_ADD_FORM_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(CHANGE_NOTICE_TO_ATTRIBUTE))
                .andExpect(model().attributeExists("changeReasonCodes"))
                .andExpect(model().attributeExists("developers"))
                .andExpect(view().name(CHANGE_NOTICE_FORM_VIEW));
    }

    @Test
    void showAddFormUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(CHANGE_NOTICES_ADD_FORM_URL))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showAddFormForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(CHANGE_NOTICES_ADD_FORM_URL))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void create() throws Exception {
        ChangeNoticeTo newChangeNoticeTo = ChangeNoticeTestData.getNewTo();
        ChangeNotice newChangeNotice = ChangeNoticeTestData.getNew();
        ResultActions actions = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, CHANGE_NOTICES_URL)
                .file(CHANGE_NOTICE_FILE)
                .params((ChangeNoticeTestData.getNewParams()))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(flash().attribute(ACTION, "Change notice " + newChangeNoticeTo.getName() + " was created"));
        ChangeNotice created = service.getWithChangesByName(newChangeNoticeTo.getName());
        newChangeNotice.setId(created.id());
        CHANGE_NOTICE_MATCHER.assertMatchIgnoreFields(created, newChangeNotice, "changes.id", "changes.document.id", "changes.document.originalHolder", "changes.document.developer");
        actions.andExpect(redirectedUrl(CHANGE_NOTICES_URL_SLASH + created.getId()));
        assertTrue(Files.exists(Paths.get(contentPath, newChangeNoticeTo.getName(), newChangeNoticeTo.getFile().getOriginalFilename())));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createWhenAutogeneratedExist() throws Exception {
        ChangeNoticeTo newChangeNoticeTo = ChangeNoticeTestData.getNewTo();
        ChangeNotice newChangeNotice = ChangeNoticeTestData.getNew();
        newChangeNoticeTo.setName(changeNotice3.getName());
        newChangeNoticeTo.setReleaseDate(changeNotice3.getReleaseDate());
        newChangeNotice.setName(changeNotice3.getName());
        newChangeNotice.setReleaseDate(changeNotice3.getReleaseDate());
        newChangeNotice.getFile().setFileLink(changeNotice3.getName() + "/" + newChangeNoticeTo.getFile().getOriginalFilename());
        MultiValueMap<String, String> newParams = ChangeNoticeTestData.getNewParams();
        newParams.set(NAME_PARAM, changeNotice3.getName());
        newParams.set("releaseDate", changeNotice3.getReleaseDate().toString());

        ResultActions actions = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, CHANGE_NOTICES_URL)
                .file(CHANGE_NOTICE_FILE)
                .params(newParams)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(flash().attribute(ACTION, "Change notice " + newChangeNoticeTo.getName() + " was created"));
        ChangeNotice created = service.getWithChangesByName(newChangeNoticeTo.getName());
        newChangeNotice.setId(created.id());
        CHANGE_NOTICE_MATCHER.assertMatchIgnoreFields(created, newChangeNotice, "changes.id", "changes.document.id", "changes.document.originalHolder", "changes.document.developer");
        actions.andExpect(redirectedUrl(CHANGE_NOTICES_URL_SLASH + created.getId()));
        assertTrue(Files.exists(Paths.get(contentPath, newChangeNoticeTo.getName(), newChangeNoticeTo.getFile().getOriginalFilename())));
    }

    @Test
    void createUnAuthorize() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, CHANGE_NOTICES_URL)
                .file(CHANGE_NOTICE_FILE)
                .params((ChangeNoticeTestData.getNewParams()))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertThrows(NotFoundException.class, () -> service.getByName(ChangeNoticeTestData.getNew().getName()));
        assertTrue(Files.notExists(Paths.get(contentPath, ChangeNoticeTestData.getNew().getFile().getFileLink())));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createForbidden() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, CHANGE_NOTICES_URL)
                .file(CHANGE_NOTICE_FILE)
                .params((ChangeNoticeTestData.getNewParams()))
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertThrows(NotFoundException.class, () -> service.getByName(ChangeNoticeTestData.getNew().getName()));
        assertTrue(Files.notExists(Paths.get(contentPath, ChangeNoticeTestData.getNew().getFile().getFileLink())));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createInvalid() throws Exception {
        MultiValueMap<String, String> newInvalidParams = ChangeNoticeTestData.getNewInvalidParams();
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, CHANGE_NOTICES_URL)
                .file(CHANGE_NOTICE_FILE)
                .params(newInvalidParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(CHANGE_NOTICE_TO_ATTRIBUTE, NAME_PARAM,
                        "releaseDate", "changes"))
                .andExpect(view().name(CHANGE_NOTICE_FORM_VIEW));
        assertThrows(NotFoundException.class, () -> service.getByName(newInvalidParams.get(NAME_PARAM).get(0)));
        assertTrue(Files.notExists(Paths.get(contentPath, newInvalidParams.get(NAME_PARAM).get(0) + "/" + CHANGE_NOTICE_FILE.getOriginalFilename())));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createDuplicateName() throws Exception {
        MultiValueMap<String, String> newParams = ChangeNoticeTestData.getNewParams();
        newParams.set(NAME_PARAM, changeNotice1.getName());
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, CHANGE_NOTICES_URL)
                .file(CHANGE_NOTICE_FILE)
                .params(newParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(CHANGE_NOTICE_TO_ATTRIBUTE, NAME_PARAM, DUPLICATE_ERROR_CODE))
                .andExpect(view().name(CHANGE_NOTICE_FORM_VIEW));
        assertNotEquals(ChangeNoticeTestData.getNew().getReleaseDate(), service.getByName(changeNotice1.getName()).getReleaseDate());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void showEditForm() throws Exception {
        perform(MockMvcRequestBuilders.get(CHANGE_NOTICES_EDIT_FORM_URL + CHANGE_NOTICE_1_ID))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(CHANGE_NOTICE_TO_ATTRIBUTE))
                .andExpect(model().attributeExists("changeReasonCodes"))
                .andExpect(model().attributeExists("developers"))
                .andExpect(model().attributeExists("file"))
                .andExpect(view().name(CHANGE_NOTICE_FORM_VIEW))
                .andExpect(result ->
                        CHANGE_NOTICE_TO_MATCHER.assertMatch((ChangeNoticeTo) Objects.requireNonNull(result.getModelAndView()).getModel().get(CHANGE_NOTICE_TO_ATTRIBUTE), changeNoticeUtil.asTo(changeNotice1)));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void showEditFormNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(CHANGE_NOTICES_EDIT_FORM_URL + NOT_FOUND))
                .andExpect(exception().exceptionPage(ENTITY_NOT_FOUND, NotFoundException.class));
    }

    @Test
    void showEditFormUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(CHANGE_NOTICES_EDIT_FORM_URL + CHANGE_NOTICE_1_ID))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showEditFormForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(CHANGE_NOTICES_EDIT_FORM_URL + CHANGE_NOTICE_1_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void update() throws Exception {
        ChangeNotice updatedChangeNotice = ChangeNoticeTestData.getUpdated();
        perform(MockMvcRequestBuilders.post(CHANGE_NOTICES_URL)
                .params(ChangeNoticeTestData.getUpdatedParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(CHANGE_NOTICES_URL_SLASH + CHANGE_NOTICE_1_ID))
                .andExpect(flash().attribute(ACTION, "Change notice " + updatedChangeNotice.getName() + " was updated"));
        CHANGE_NOTICE_MATCHER.assertMatchIgnoreFields(service.getWithChanges(CHANGE_NOTICE_1_ID), updatedChangeNotice, "changes.id", "changes.document.id", "changes.document.originalHolder", "changes.document.developer");
        assertTrue(Files.exists(Paths.get(contentPath, updatedChangeNotice.getFile().getFileLink())));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void updateToAutoGenerateName() throws Exception {
        ChangeNotice updatedChangeNotice = ChangeNoticeTestData.getUpdated();
        updatedChangeNotice.setName(changeNotice3.getName());
        MultiValueMap<String, String> updatedParams = ChangeNoticeTestData.getUpdatedParams();
        updatedParams.set(NAME_PARAM, changeNotice3.getName());
        perform(MockMvcRequestBuilders.post(CHANGE_NOTICES_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(CHANGE_NOTICES_URL_SLASH + CHANGE_NOTICE_1_ID))
                .andExpect(flash().attribute(ACTION, "Change notice " + updatedChangeNotice.getName() + " was updated"));
        CHANGE_NOTICE_MATCHER.assertMatchIgnoreFields(service.getWithChanges(CHANGE_NOTICE_1_ID), updatedChangeNotice, "changes.id", "changes.document.id", "changes.document.originalHolder", "changes.document.developer");
        assertTrue(Files.exists(Paths.get(contentPath, updatedChangeNotice.getFile().getFileLink())));
        assertThrows(NotFoundException.class, () -> service.get(CHANGE_NOTICE_3_ID));
    }

    //Check UniqueNameValidator works correct when update
    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void updateNameNotChange() throws Exception {
        ChangeNotice updatedChangeNotice = ChangeNoticeTestData.getUpdated();
        updatedChangeNotice.setName(changeNotice1.getName());
        MultiValueMap<String, String> updatedParams = ChangeNoticeTestData.getUpdatedParams();
        updatedParams.set(NAME_PARAM, changeNotice1.getName());
        perform(MockMvcRequestBuilders.post(CHANGE_NOTICES_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(CHANGE_NOTICES_URL_SLASH + CHANGE_NOTICE_1_ID))
                .andExpect(flash().attribute(ACTION, "Change notice " + updatedChangeNotice.getName() + " was updated"));
        CHANGE_NOTICE_MATCHER.assertMatchIgnoreFields(service.getWithChanges(CHANGE_NOTICE_1_ID), updatedChangeNotice, "changes.id", "changes.document.id", "changes.document.originalHolder", "changes.document.developer");
        assertTrue(Files.exists(Paths.get(contentPath, updatedChangeNotice.getFile().getFileLink())));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void updateNotFound() throws Exception {
        MultiValueMap<String, String> updatedParams = ChangeNoticeTestData.getUpdatedParams();
        updatedParams.set(ID, NOT_FOUND + "");
        perform(MockMvcRequestBuilders.post(CHANGE_NOTICES_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(exception().exceptionPage(ENTITY_NOT_FOUND, NotFoundException.class));
    }

    @Test
    void updateUnAuthorize() throws Exception {
        perform(MockMvcRequestBuilders.post(CHANGE_NOTICES_URL)
                .params(ChangeNoticeTestData.getUpdatedParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertNotEquals(service.get(CHANGE_NOTICE_1_ID).getName(), ChangeNoticeTestData.getUpdated().getName());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateForbidden() throws Exception {
        perform(MockMvcRequestBuilders.post(CHANGE_NOTICES_URL)
                .params(ChangeNoticeTestData.getUpdatedParams())
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertNotEquals(service.get(CHANGE_NOTICE_1_ID).getName(), ChangeNoticeTestData.getUpdated().getName());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void updateInvalid() throws Exception {
        MultiValueMap<String, String> updatedInvalidParams = ChangeNoticeTestData.getUpdatedInvalidParams();
        perform(MockMvcRequestBuilders.post(CHANGE_NOTICES_URL)
                .params(updatedInvalidParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(CHANGE_NOTICE_TO_ATTRIBUTE, NAME_PARAM,
                        "releaseDate", "changes"))
                .andExpect(view().name(CHANGE_NOTICE_FORM_VIEW));
        assertNotEquals(service.get(CHANGE_NOTICE_1_ID).getName(), updatedInvalidParams.get(NAME_PARAM).get(0));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void updateDuplicateName() throws Exception {
        MultiValueMap<String, String> updatedParams = ChangeNoticeTestData.getUpdatedParams();
        updatedParams.set(NAME_PARAM, changeNotice2.getName());
        perform(MockMvcRequestBuilders.post(CHANGE_NOTICES_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(CHANGE_NOTICE_TO_ATTRIBUTE, NAME_PARAM, DUPLICATE_ERROR_CODE))
                .andExpect(view().name(CHANGE_NOTICE_FORM_VIEW));
        assertNotEquals(service.get(CHANGE_NOTICE_1_ID).getName(), changeNotice2.getName());
    }
}
