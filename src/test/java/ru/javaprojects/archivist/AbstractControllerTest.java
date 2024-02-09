package ru.javaprojects.archivist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest
@Sql(scripts = "classpath:data.sql", config = @SqlConfig(encoding = "UTF-8"))
@AutoConfigureMockMvc
@ActiveProfiles({"dev", "test"})
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
                assertEquals(message, model.get("message"));
                assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), model.get("status"));
                assertEquals("error/exception", result.getModelAndView().getViewName());
                assertEquals(exceptionClass, Objects.requireNonNull(result.getResolvedException()).getClass());
            };
        }
    }
}
