package ru.javaprojects.archivist.users.web;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaprojects.archivist.AbstractControllerTest;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.archivist.users.web.LoginController.LOGIN_URL;
import static ru.javaprojects.archivist.users.UserTestData.USER_MAIL;

class LoginControllerTest extends AbstractControllerTest {

    @Test
    void showLoginPageUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(LOGIN_URL))
                .andExpect(status().isOk())
                .andExpect(view().name("users/login"));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showLoginPageAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(LOGIN_URL))
                .andExpect(status().isFound())
                .andExpect(redirectedUrl("/"));
    }
}
