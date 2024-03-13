package ru.javaprojects.archivist.globalsearch;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import ru.javaprojects.archivist.AbstractControllerTest;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.javaprojects.archivist.CommonTestData.USER_MAIL;
import static ru.javaprojects.archivist.common.web.PathUIController.LOGIN_URL;
import static ru.javaprojects.archivist.globalsearch.GlobalSearchUIController.SEARCH_URL;

class GlobalSearchUIControllerTest extends AbstractControllerTest {
    private static final String SEARCH_RESULTS_ATTRIBUTE = "searchResults";
    private static final String SEARCH_RESULTS_LIMIT_ATTRIBUTE = "searchResultsLimit";
    private static final String SEARCH_RESULTS_VIEW = "global-search/search-results";

    private static final String GS_KEYWORD = "gsKeyword";

    @Value("${search-results-limit}")
    private Integer searchResultsLimit;

    @Test
    @WithUserDetails(USER_MAIL)
    @SuppressWarnings("unchecked")
    void getSearchResults() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(SEARCH_URL)
                .param(GS_KEYWORD, "vuia.4"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(SEARCH_RESULTS_ATTRIBUTE))
                .andExpect(model().attributeExists(SEARCH_RESULTS_LIMIT_ATTRIBUTE))
                .andExpect(view().name(SEARCH_RESULTS_VIEW));

        List<SearchResult> searchResults = (List<SearchResult>) Objects.requireNonNull(actions.andReturn().getModelAndView())
                .getModel().get(SEARCH_RESULTS_ATTRIBUTE);
        assertEquals(6, searchResults.size());
    }

    @Test
    void getSearchResultsUnAuthorized() throws Exception {
        perform(MockMvcRequestBuilders.get(SEARCH_URL)
                .param(GS_KEYWORD, "vuia"))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertTrue(Objects.requireNonNull(result.getResponse().getRedirectedUrl()).endsWith(LOGIN_URL)));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    void getSearchResultsWithEmptyKeyword() throws Exception {
        perform(MockMvcRequestBuilders.get(SEARCH_URL)
                .param(GS_KEYWORD, ""))
                .andExpect(status().isFound())
                .andExpect(result ->
                        assertEquals("/", Objects.requireNonNull(result.getResponse().getRedirectedUrl())));
    }

    @Test
    @WithUserDetails(USER_MAIL)
    @SuppressWarnings("unchecked")
    void getSearchResultsWhenMatchLimitExceeded() throws Exception {
        ResultActions actions = perform(MockMvcRequestBuilders.get(SEARCH_URL)
                .param(GS_KEYWORD, "vuia"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists(SEARCH_RESULTS_ATTRIBUTE))
                .andExpect(model().attributeExists(SEARCH_RESULTS_LIMIT_ATTRIBUTE))
                .andExpect(view().name(SEARCH_RESULTS_VIEW));

        List<SearchResult> searchResults = (List<SearchResult>) Objects.requireNonNull(actions.andReturn().getModelAndView())
                .getModel().get(SEARCH_RESULTS_ATTRIBUTE);
        assertEquals(searchResultsLimit, searchResults.size());
    }
}
