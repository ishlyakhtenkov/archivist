package ru.javaprojects.archivist.common.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import ru.javaprojects.archivist.ArchivistApplication;
import ru.javaprojects.archivist.users.Role;
import ru.javaprojects.archivist.users.User;
import ru.javaprojects.archivist.users.UserRepository;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {
    public static final PasswordEncoder PASSWORD_ENCODER = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    private final UserRepository repository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PASSWORD_ENCODER;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> {
            User user = repository.findByEmailIgnoreCase(email)
                    .orElseThrow(() -> new UsernameNotFoundException("User '" + email + "' was not found"));
            return new ArchivistApplication.AuthUser(user);
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests()
                .requestMatchers("/users/**").hasRole(Role.ADMIN.name())
                .requestMatchers("/companies/add", "/companies/create", "/companies/edit/**", "/companies/update")
                .hasAnyRole(Role.ADMIN.name(), Role.ARCHIVIST.name())
                .requestMatchers(HttpMethod.DELETE, "/companies/**").hasAnyRole(Role.ADMIN.name(), Role.ARCHIVIST.name())
                .requestMatchers("/departments/add", "/departments/create", "/departments/edit/**", "/departments/update", "/departments/delete/**")
                .hasAnyRole(Role.ADMIN.name(), Role.ARCHIVIST.name())
                .requestMatchers("/documents/add", "/documents/edit/**", "/documents/delete/**").hasAnyRole(Role.ADMIN.name(), Role.ARCHIVIST.name())
                .requestMatchers(HttpMethod.POST, "/documents").hasAnyRole(Role.ADMIN.name(), Role.ARCHIVIST.name())
                .requestMatchers(HttpMethod.DELETE, "/documents/**").hasAnyRole(Role.ADMIN.name(), Role.ARCHIVIST.name())
                .requestMatchers("/profile/forgotPassword", "/profile/resetPassword").anonymous()
                .requestMatchers(HttpMethod.POST, "/documents/applicabilities").hasAnyRole(Role.ADMIN.name(), Role.ARCHIVIST.name())
                .requestMatchers(HttpMethod.DELETE, "/documents/applicabilities/**").hasAnyRole(Role.ADMIN.name(), Role.ARCHIVIST.name())
                .requestMatchers(HttpMethod.POST, "/documents/content").hasAnyRole(Role.ADMIN.name(), Role.ARCHIVIST.name())
                .requestMatchers(HttpMethod.DELETE, "/documents/content/**").hasAnyRole(Role.ADMIN.name(), Role.ARCHIVIST.name())
                .requestMatchers(HttpMethod.POST, "/documents/sendings").hasAnyRole(Role.ADMIN.name(), Role.ARCHIVIST.name())
                .requestMatchers(HttpMethod.DELETE, "/documents/sendings/**").hasAnyRole(Role.ADMIN.name(), Role.ARCHIVIST.name())
                .requestMatchers(HttpMethod.PATCH, "/documents/subscribers/**").hasAnyRole(Role.ADMIN.name(), Role.ARCHIVIST.name())
                .requestMatchers(HttpMethod.POST, "/documents/changes").hasAnyRole(Role.ADMIN.name(), Role.ARCHIVIST.name())
                .requestMatchers(HttpMethod.DELETE, "/documents/changes/**").hasAnyRole(Role.ADMIN.name(), Role.ARCHIVIST.name())
                .requestMatchers("/", "/webjars/**", "/css/**", "/images/**", "/js/**").permitAll()
                .anyRequest().authenticated()
                .and().formLogin().permitAll()
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .and().logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .deleteCookies("JSESSIONID")
                .and().rememberMe().key("remember-me-key").rememberMeCookieName("archivist-remember-me");
//                .and().csrf().disable(); //remove
        return http.build();
    }
}
