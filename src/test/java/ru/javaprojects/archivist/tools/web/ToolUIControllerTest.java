package ru.javaprojects.archivist.tools.web;

import org.junit.jupiter.api.Test;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaprojects.archivist.AbstractControllerTest;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import static ru.javaprojects.archivist.CommonTestData.ARCHIVIST_MAIL;
import static ru.javaprojects.archivist.CommonTestData.USER_MAIL;
import static ru.javaprojects.archivist.common.web.PathUIController.LOGIN_URL;
import static ru.javaprojects.archivist.tools.web.ToolUIController.TOOLS_URL;

class ToolUIControllerTest extends AbstractControllerTest {
    private static final String TOOLS_PAGE_VIEW = "tools/tools";


    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void showToolsPage() throws Exception {
        perform(MockMvcRequestBuilders.get(TOOLS_URL))
                .andExpect(status().isOk())
                .andExpect(view().name(TOOLS_PAGE_VIEW));

    }

    @Test
    void showToolsPageUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(TOOLS_URL))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void showToolsPageForbidden() throws Exception {
        perform(MockMvcRequestBuilders.get(TOOLS_URL)
                .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
