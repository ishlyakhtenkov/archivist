package ru.javaprojects.archivist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@Transactional
@AutoConfigureMockMvc
public abstract class AbstractControllerTest {

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

    public static class ExceptionResultMatchers {

        public static ExceptionResultMatchers exception(){
            return new ExceptionResultMatchers();
        }

        public ResultMatcher exceptionPage(String message, Class<?> exceptionClass) {
            return result -> {
                Map<String, Object> model = Objects.requireNonNull(result.getModelAndView()).getModel();
                assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getResponse().getStatus());
                assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(), model.get("typeMessage"));
                assertNotNull(model.get("exception"));
                assertEquals(message, model.get("message"));
                assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), model.get("status"));
                assertEquals("error/exception", result.getModelAndView().getViewName());
                assertEquals(exceptionClass, Objects.requireNonNull(result.getResolvedException()).getClass());
            };
        }
    }

    protected void assertExceptionPage(ResultActions actions, String message, Class<?> exceptionType) throws Exception {
        actions
                .andExpect(status().isInternalServerError())
                .andExpect(model().attribute("typeMessage", "Internal Server Error"))
                .andExpect(model().attributeExists("exception"))
                .andExpect(model().attribute("message", message))
                .andExpect(model().attribute("status", HttpStatus.INTERNAL_SERVER_ERROR.value()))
                .andExpect(view().name("error/exception"))
                .andExpect(result ->
                        assertEquals(exceptionType, Objects.requireNonNull(result.getResolvedException()).getClass()));
    }
}
