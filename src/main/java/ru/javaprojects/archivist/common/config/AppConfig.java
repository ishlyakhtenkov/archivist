package ru.javaprojects.archivist.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.thymeleaf.dialect.springdata.SpringDataDialect;
import ru.javaprojects.archivist.common.web.JacksonObjectMapper;

@Configuration
public class AppConfig {

    @Bean
    public SpringDataDialect springDataDialect() {
        return new SpringDataDialect();
    }

    @Bean
    public ObjectMapper objectMapper() {
        return JacksonObjectMapper.getMapper();
    }
}
