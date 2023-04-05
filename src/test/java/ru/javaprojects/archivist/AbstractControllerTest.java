package ru.javaprojects.archivist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public class AbstractControllerTest {

    @Autowired
    private MockMvc mockMvc;

    protected ResultActions perform(MockHttpServletRequestBuilder builder) throws Exception {
        return mockMvc.perform(builder);
    }

    protected ResultMatcher problemTitle(String title) {
        return jsonPath("$.title").value(title);
    }

    protected ResultMatcher problemStatus(int status) {
        return jsonPath("$.status").value(status);
    }

    protected ResultMatcher problemDetail(String detail) {
        return jsonPath("$.detail").value(detail);
    }

    protected ResultMatcher problemInstance(String instance) {
        return jsonPath("$.instance").value(instance);
    }
}
