package ru.javaprojects.archivist.companies.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaprojects.archivist.AbstractControllerTest;
import ru.javaprojects.archivist.common.error.exception.NotFoundException;
import ru.javaprojects.archivist.companies.CompanyRepository;
import ru.javaprojects.archivist.companies.CompanyService;
import ru.javaprojects.archivist.companies.CompanyTestData;
import ru.javaprojects.archivist.companies.model.Address;
import ru.javaprojects.archivist.companies.model.Company;
import ru.javaprojects.archivist.companies.model.Contacts;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.archivist.CommonTestData.KEYWORD;
import static ru.javaprojects.archivist.CommonTestData.getPageableParams;
import static ru.javaprojects.archivist.companies.CompanyTestData.*;
import static ru.javaprojects.archivist.companies.web.CompanyUIController.COMPANIES_URL;
import static ru.javaprojects.archivist.users.UserTestData.*;
import static ru.javaprojects.archivist.users.web.LoginController.LOGIN_URL;

class CompanyControllerTest extends AbstractControllerTest {
    private static final String COMPANIES_ADD_FORM_URL = COMPANIES_URL + "/add";
    private static final String COMPANIES_CREATE_URL = COMPANIES_URL + "/create";

    private static final String COMPANIES_VIEW = "companies/companies";
    private static final String COMPANIES_ADD_VIEW = "companies/company-form";

    @Autowired
    private CompanyService service;

    @Autowired
    private CompanyRepository repository;

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
        COMPANY_MATCHER.assertMatch(companies.getContent(), List.of(company2, company3));
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
        COMPANY_MATCHER.assertMatch(companies.getContent(), List.of(company1));
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
    void showAddForm() throws Exception {
        perform(MockMvcRequestBuilders.get(COMPANIES_ADD_FORM_URL))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(COMPANY_ATTRIBUTE))
                .andExpect(view().name(COMPANIES_ADD_VIEW))
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
        Company created = repository.findByNameIgnoreCase(newCompany.getName())
                .orElseThrow(() -> new NotFoundException("Not found company with name =" + newCompany.getName()));
        newCompany.setId(created.id());
        COMPANY_MATCHER.assertMatch(created, newCompany);
    }
}