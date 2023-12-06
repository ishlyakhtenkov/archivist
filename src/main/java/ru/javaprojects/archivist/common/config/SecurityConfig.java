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
import ru.javaprojects.archivist.users.AuthUser;
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
            return new AuthUser(user);
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests((authorizeHttpRequests) ->
                        authorizeHttpRequests
                                .requestMatchers("/users/**").hasRole(Role.ADMIN.name())
                                .requestMatchers("/profile/forgotPassword", "/profile/resetPassword").anonymous()
                                .requestMatchers(HttpMethod.POST).hasAnyRole(Role.ADMIN.name(), Role.ARCHIVIST.name())
                                .requestMatchers(HttpMethod.PATCH, "/documents/subscribers/**").hasAnyRole(Role.ADMIN.name(), Role.ARCHIVIST.name())
                                .requestMatchers(HttpMethod.DELETE).hasAnyRole(Role.ADMIN.name(), Role.ARCHIVIST.name())
                                .requestMatchers(HttpMethod.GET, "*/edit/**", "*/add").hasAnyRole(Role.ADMIN.name(), Role.ARCHIVIST.name())
                                .requestMatchers("/", "/webjars/**", "/css/**", "/images/**", "/js/**").permitAll()
                                .anyRequest().authenticated()
                )
                .formLogin((formLogin) ->
                        formLogin
                                .permitAll()
                                .loginPage("/login")
                                .defaultSuccessUrl("/", true)
                )
                .logout((logout) ->
                        logout
                                .logoutUrl("/logout")
                                .logoutSuccessUrl("/")
                                .invalidateHttpSession(true)
                                .clearAuthentication(true)
                                .deleteCookies("JSESSIONID")
                ).rememberMe((rememberMe) ->
                        rememberMe
                                .key("remember-me-key")
                                .rememberMeCookieName("archivist-remember-me"));
        return http.build();
    }
}
