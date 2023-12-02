package ru.javaprojects.archivist.companies.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.util.MultiValueMap;
import ru.javaprojects.archivist.AbstractControllerTest;
import ru.javaprojects.archivist.common.error.NotFoundException;
import ru.javaprojects.archivist.companies.CompanyService;
import ru.javaprojects.archivist.companies.CompanyTestData;
import ru.javaprojects.archivist.companies.model.Address;
import ru.javaprojects.archivist.companies.model.Company;
import ru.javaprojects.archivist.companies.model.Contacts;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.archivist.AbstractControllerTest.ExceptionResultMatchers.exception;
import static ru.javaprojects.archivist.CommonTestData.*;
import static ru.javaprojects.archivist.common.error.Constants.DUPLICATE_ERROR_CODE;
import static ru.javaprojects.archivist.companies.CompanyTestData.*;
import static ru.javaprojects.archivist.companies.web.CompanyUIController.COMPANIES_URL;
import static ru.javaprojects.archivist.users.web.LoginController.LOGIN_URL;

class CompanyUIControllerTest extends AbstractControllerTest {
    private static final String COMPANIES_ADD_FORM_URL = COMPANIES_URL + "/add";
    private static final String COMPANIES_CREATE_URL = COMPANIES_URL + "/create";
    private static final String COMPANIES_EDIT_FORM_URL = COMPANIES_URL + "/edit/";
    private static final String COMPANIES_UPDATE_URL = COMPANIES_URL + "/update";
    private static final String COMPANIES_URL_SLASH = COMPANIES_URL + "/";

    private static final String COMPANIES_VIEW = "companies/companies";
    private static final String COMPANY_VIEW = "companies/company";
    private static final String COMPANIES_FORM_VIEW = "companies/company-form";

    @Autowired
    private CompanyService service;

    @Test
    @WithUserDetails(USER_MAIL)
    @SuppressWarnings("unchecked")
    void getAll() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(COMPANIES_URL)
                .params(getPageableParams()))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(COMPANIES_ATTRIBUTE))
                .andExpect(view().name(COMPANIES_VIEW));
        Page<Company> companies = (Page<Company>) Objects.requireNonNull(actions.andReturn().getModelAndView())
                .getModel().get(COMPANIES_ATTRIBUTE);
        assertEquals(3, companies.getTotalElements());
        assertEquals(2, companies.getTotalPages());
        COMPANY_MATCHER.assertMatchIgnoreFields(companies.getContent(), List.of(company2, company3), "contactPersons");
    }

    @Test
    @WithUserDetails(ADMIN_MAIL)
    @SuppressWarnings("unchecked")
    void getAllByKeyword() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(COMPANIES_URL)
                .param(KEYWORD, "ttk"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(COMPANIES_ATTRIBUTE))
                .andExpect(view().name(COMPANIES_VIEW));
        Page<Company> companies = (Page<Company>) Objects.requireNonNull(actions.andReturn().getModelAndView())
                .getModel().get(COMPANIES_ATTRIBUTE);
        assertEquals(1, companies.getTotalElements());
        assertEquals(1, companies.getTotalPages());
        COMPANY_MATCHER.assertMatchIgnoreFields(companies.getContent(), List.of(company1), "contactPersons");
    }

    @Test
    void getAllUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(COMPANIES_URL)
                .params(getPageableParams()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void get() throws Exception {
        perform(MockMvcRequestBuilders.get(COMPANIES_URL_SLASH + COMPANY1_ID))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(COMPANY_ATTRIBUTE))
                .andExpect(view().name(COMPANY_VIEW))
                .andExpect(result ->
                        COMPANY_MATCHER.assertMatch((Company) Objects.requireNonNull(result.getModelAndView()).getModel().get(COMPANY_ATTRIBUTE), company1));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void getNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(COMPANIES_URL_SLASH + NOT_FOUND))
                .andExpect(exception().exceptionPage(ENTITY_NOT_FOUND, NotFoundException.class));
    }

    @Test
    void getUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(COMPANIES_URL_SLASH + COMPANY1_ID))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void showAddForm() throws Exception {
        perform(MockMvcRequestBuilders.get(COMPANIES_ADD_FORM_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(COMPANY_ATTRIBUTE))
                .andExpect(view().name(COMPANIES_FORM_VIEW))
                .andExpect(result ->
                        COMPANY_MATCHER.assertMatch((Company) Objects.requireNonNull(result.getModelAndView()).getModel().get(COMPANY_ATTRIBUTE),
                                new Company(null, null, new Address(), new Contacts(), null)));
    }

    @Test
    void showAddFormUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(COMPANIES_ADD_FORM_URL))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showAddFormForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(COMPANIES_ADD_FORM_URL))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void create() throws Exception {
        Company newCompany = CompanyTestData.getNew();
        perform(MockMvcRequestBuilders.post(COMPANIES_CREATE_URL)
                .params((CompanyTestData.getNewParams()))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(COMPANIES_URL))
                .andExpect(flash().attribute(ACTION, "Company " + newCompany.getName() + " was created"));
        Company created = service.getByName(newCompany.getName());
        newCompany.setId(created.id());
        COMPANY_MATCHER.assertMatch(created, newCompany);
    }

    @Test
    void createUnAuthorize() throws Exception {
        perform(MockMvcRequestBuilders.post(COMPANIES_CREATE_URL)
                .params((CompanyTestData.getNewParams()))
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertThrows(NotFoundException.class, () -> service.getByName(CompanyTestData.getNew().getName()));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void createForbidden() throws Exception {
        perform(MockMvcRequestBuilders.post(COMPANIES_CREATE_URL)
                .params((CompanyTestData.getNewParams()))
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertThrows(NotFoundException.class, () -> service.getByName(CompanyTestData.getNew().getName()));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createInvalid() throws Exception {
        MultiValueMap<String, String> newInvalidParams = CompanyTestData.getNewInvalidParams();
        perform(MockMvcRequestBuilders.post(COMPANIES_CREATE_URL)
                .params(newInvalidParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(COMPANY_ATTRIBUTE, NAME_PARAM, COUNTRY_PARAM, ZIPCODE_PARAM,
                        CITY_PARAM, STREET_PARAM, HOUSE_PARAM, CONTACT_PERSON_POSITION_PARAM, CONTACT_PERSON_LAST_NAME_PARAM,
                        CONTACT_PERSON_FIRST_NAME_PARAM, CONTACT_PERSON_MIDDLE_NAME_PARAM))
                .andExpect(view().name(COMPANIES_FORM_VIEW));
        assertThrows(NotFoundException.class, () -> service.getByName(newInvalidParams.get(NAME_PARAM).get(0)));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void createDuplicateName() throws Exception {
        MultiValueMap<String, String> newParams = CompanyTestData.getNewParams();
        newParams.set(NAME_PARAM, COMPANY1_NAME);
        perform(MockMvcRequestBuilders.post(COMPANIES_CREATE_URL)
                .params(newParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(COMPANY_ATTRIBUTE, NAME_PARAM, DUPLICATE_ERROR_CODE))
                .andExpect(view().name(COMPANIES_FORM_VIEW));
        assertNotEquals(CompanyTestData.getNew().getAddress(), service.getByName(COMPANY1_NAME).getAddress());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void showEditForm() throws Exception {
        perform(MockMvcRequestBuilders.get(COMPANIES_EDIT_FORM_URL + COMPANY1_ID))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(COMPANY_ATTRIBUTE))
                .andExpect(view().name(COMPANIES_FORM_VIEW))
                .andExpect(result ->
                        COMPANY_MATCHER.assertMatch((Company) Objects.requireNonNull(result.getModelAndView()).getModel().get(COMPANY_ATTRIBUTE), company1));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void showEditFormNotFound() throws Exception {
        perform(MockMvcRequestBuilders.get(COMPANIES_EDIT_FORM_URL + NOT_FOUND))
                .andExpect(exception().exceptionPage(ENTITY_NOT_FOUND, NotFoundException.class));
    }

    @Test
    void showEditFormUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(COMPANIES_EDIT_FORM_URL + COMPANY1_ID))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showEditFormForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(COMPANIES_EDIT_FORM_URL + COMPANY1_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void update() throws Exception {
        Company updatedCompany = CompanyTestData.getUpdated();
        perform(MockMvcRequestBuilders.post(COMPANIES_UPDATE_URL)
                .params(CompanyTestData.getUpdatedParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(COMPANIES_URL_SLASH + COMPANY1_ID))
                .andExpect(flash().attribute(ACTION, "Company " + updatedCompany.getName() + " was updated"));
        COMPANY_MATCHER.assertMatch(service.get(COMPANY1_ID), updatedCompany);
    }

    //Check UniqueNameValidator works correct when update
    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void updateNameNotChange() throws Exception {
        Company updatedCompany = CompanyTestData.getUpdated();
        updatedCompany.setName(COMPANY1_NAME);
        MultiValueMap<String, String> updatedParams = CompanyTestData.getUpdatedParams();
        updatedParams.set(NAME_PARAM, COMPANY1_NAME);
        perform(MockMvcRequestBuilders.post(COMPANIES_UPDATE_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl(COMPANIES_URL_SLASH + COMPANY1_ID))
                .andExpect(flash().attribute(ACTION, "Company " + updatedCompany.getName() + " was updated"));
        COMPANY_MATCHER.assertMatch(service.get(COMPANY1_ID), updatedCompany);
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void updateNotFound() throws Exception {
        MultiValueMap<String, String> updatedParams = CompanyTestData.getUpdatedParams();
        updatedParams.set(ID, NOT_FOUND + "");
        perform(MockMvcRequestBuilders.post(COMPANIES_UPDATE_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(exception().exceptionPage(ENTITY_NOT_FOUND, NotFoundException.class));
    }

    @Test
    void updateUnAuthorize() throws Exception {
        perform(MockMvcRequestBuilders.post(COMPANIES_UPDATE_URL)
                .params(CompanyTestData.getUpdatedParams())
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
        assertNotEquals(service.get(COMPANY1_ID).getName(), CompanyTestData.getUpdated().getName());
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void updateForbidden() throws Exception {
        perform(MockMvcRequestBuilders.post(COMPANIES_UPDATE_URL)
                .params(CompanyTestData.getUpdatedParams())
                .with(csrf()))
                .andExpect(status().isForbidden());
        assertNotEquals(service.get(COMPANY1_ID).getName(), CompanyTestData.getUpdated().getName());
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void updateInvalid() throws Exception {
        MultiValueMap<String, String> updatedInvalidParams = CompanyTestData.getUpdatedInvalidParams();
        perform(MockMvcRequestBuilders.post(COMPANIES_UPDATE_URL)
                .params(updatedInvalidParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors(COMPANY_ATTRIBUTE, NAME_PARAM, COUNTRY_PARAM, ZIPCODE_PARAM,
                        CITY_PARAM, STREET_PARAM, HOUSE_PARAM, CONTACT_PERSON_POSITION_PARAM, CONTACT_PERSON_LAST_NAME_PARAM,
                        CONTACT_PERSON_FIRST_NAME_PARAM, CONTACT_PERSON_MIDDLE_NAME_PARAM))
                .andExpect(view().name(COMPANIES_FORM_VIEW));
        assertNotEquals(service.get(COMPANY1_ID).getName(), updatedInvalidParams.get(NAME_PARAM).get(0));
    }

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void updateDuplicateName() throws Exception {
        MultiValueMap<String, String> updatedParams = CompanyTestData.getUpdatedParams();
        updatedParams.set(NAME_PARAM, COMPANY2_NAME);
        perform(MockMvcRequestBuilders.post(COMPANIES_UPDATE_URL)
                .params(updatedParams)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrorCode(COMPANY_ATTRIBUTE, NAME_PARAM, DUPLICATE_ERROR_CODE))
                .andExpect(view().name(COMPANIES_FORM_VIEW));
        assertNotEquals(service.get(COMPANY1_ID).getName(), COMPANY2_NAME);
    }
}
