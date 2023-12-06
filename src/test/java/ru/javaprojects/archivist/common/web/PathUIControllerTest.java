package ru.javaprojects.archivist.common.web;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaprojects.archivist.AbstractControllerTest;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.archivist.CommonTestData.USER_MAIL;
import static ru.javaprojects.archivist.common.web.PathUIController.LOGIN_URL;

class PathUIControllerTest extends AbstractControllerTest {

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
