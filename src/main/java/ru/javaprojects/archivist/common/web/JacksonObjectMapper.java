package ru.javaprojects.archivist.common.web;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.hibernate5.jakarta.Hibernate5JakartaModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.http.ProblemDetail;

import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.ANY;
import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;

public class JacksonObjectMapper extends ObjectMapper {
    private static final ObjectMapper MAPPER = new JacksonObjectMapper();

    //    https://stackoverflow.com/a/74630129/548473
    @JsonAutoDetect(fieldVisibility = NONE, getterVisibility = ANY)
    interface MixIn {
        @JsonAnyGetter
        Map<String, Object> getProperties();
    }

    private JacksonObjectMapper() {
        registerModule(new Hibernate5JakartaModule());
        registerModule(new JavaTimeModule());
        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        addMixIn(ProblemDetail.class, MixIn.class);
        setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

    public static ObjectMapper getMapper() {
        return MAPPER;
    }
}
