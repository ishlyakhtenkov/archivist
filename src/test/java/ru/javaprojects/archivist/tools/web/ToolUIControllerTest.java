package ru.javaprojects.archivist.tools.web;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaprojects.archivist.AbstractControllerTest;

import java.io.ByteArrayInputStream;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.archivist.CommonTestData.ARCHIVIST_MAIL;
import static ru.javaprojects.archivist.CommonTestData.USER_MAIL;
import static ru.javaprojects.archivist.common.web.PathUIController.LOGIN_URL;
import static ru.javaprojects.archivist.documents.DocumentTestData.*;
import static ru.javaprojects.archivist.tools.ToolsTestData.DECIMAL_NUMBERS_LIST_FILE;
import static ru.javaprojects.archivist.tools.ToolsTestData.GROUP_CONTENT_DOWNLOAD_OPERATION_RESULT_FILE_CONTENT;
import static ru.javaprojects.archivist.tools.web.ToolUIController.*;

class ToolUIControllerTest extends AbstractControllerTest {
    private static final String TOOLS_PAGE_VIEW = "tools/tools";
    private static final String GROUP_CONTENT_DOWNLOAD_URL =TOOLS_URL + "/group/content/download";


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

    @Test
    @WithUserDetails(ARCHIVIST_MAIL)
    void groupContentDownload() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, GROUP_CONTENT_DOWNLOAD_URL)
                .file(DECIMAL_NUMBERS_LIST_FILE)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(ZIP_CONTENT_TYPE))
                .andExpect(header().string(CONTENT_DISPOSITION_HEADER, CONTENT_DISPOSITION_HEADER_VALUE));
        Set<String> zipFileNames = new HashSet<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(actions.andReturn().getResponse().getContentAsByteArray()))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                byte[] bytes = zis.readAllBytes();
                if (zipEntry.getName().equals(OPERATION_RESULT_FILE_NAME)) {
                    assertEquals(GROUP_CONTENT_DOWNLOAD_OPERATION_RESULT_FILE_CONTENT, new String(bytes));
                }
                assertTrue(bytes.length > 0);
                zipFileNames.add(zipEntry.getName());
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
        }
        assertEquals(4, zipFileNames.size());
        assertTrue(zipFileNames.contains(OPERATION_RESULT_FILE_NAME));
        assertTrue(zipFileNames.contains(Paths.get(document1.getDecimalNumber(), content3.getFiles().get(0).getFileName()).toString()));
        assertTrue(zipFileNames.contains(Paths.get(document2.getDecimalNumber(), "List_1.pdf").toString()));
        assertTrue(zipFileNames.contains(Paths.get(document2.getDecimalNumber(), "List_2.pdf").toString()));
    }

    @Test
    void groupContentDownloadUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, GROUP_CONTENT_DOWNLOAD_URL)
                .file(DECIMAL_NUMBERS_LIST_FILE)
                .with(csrf()))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void groupContentDownloadForbidden() throws Exception {
        perform(MockMvcRequestBuilders.multipart(HttpMethod.POST, GROUP_CONTENT_DOWNLOAD_URL)
                .file(DECIMAL_NUMBERS_LIST_FILE)
                .with(csrf()))
                .andExpect(status().isForbidden());
    }
}
