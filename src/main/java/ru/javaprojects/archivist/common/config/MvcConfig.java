package ru.javaprojects.archivist.common.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.WebRequestInterceptor;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.WebRequestHandlerInterceptorAdapter;
import ru.javaprojects.archivist.ArchivistApplication;
import ru.javaprojects.archivist.ArchivistApplication.AuthUser;

@EnableAutoConfiguration
@Configuration
public class MvcConfig implements WebMvcConfigurer {

    // Add authUser to view model
    private final HandlerInterceptor authInterceptor = new WebRequestHandlerInterceptorAdapter(new WebRequestInterceptor() {
        @Override
        public void postHandle(WebRequest request, ModelMap model) {
            if (model != null) {
                AuthUser authUser = ArchivistApplication.AuthUser.safeGet();
                if (authUser != null) {
                    model.addAttribute("authUser", authUser);
                }
            }
        }

        @Override
        public void afterCompletion(WebRequest request, Exception ex) {
        }

        @Override
        public void preHandle(WebRequest request) {
        }
    });

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor).excludePathPatterns("/js/**", "/webjars/**", "/css/**", "/images/**");
    }
}
