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
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import ru.javaprojects.archivist.common.error.NotFoundException;
import ru.javaprojects.archivist.users.AuthUser;
import ru.javaprojects.archivist.users.Role;
import ru.javaprojects.archivist.users.User;
import ru.javaprojects.archivist.users.UserService;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {
    public static final PasswordEncoder PASSWORD_ENCODER = PasswordEncoderFactories.createDelegatingPasswordEncoder();

    private final UserService userService;
    private final UserMdcFilter userMdcFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PASSWORD_ENCODER;
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> {
            try {
                User user = userService.getByEmail(email);
                return new AuthUser(user);
            } catch (NotFoundException ex) {
                throw new UsernameNotFoundException(ex.getMessage());
            }
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .addFilterAfter(userMdcFilter, AuthorizationFilter.class)
                .authorizeHttpRequests((authorizeHttpRequests) ->
                        authorizeHttpRequests
                                .requestMatchers("/users/**", "/posts/**").hasRole(Role.ADMIN.name())
                                .requestMatchers("/profile/forgotPassword", "/profile/resetPassword").anonymous()
                                .requestMatchers(HttpMethod.POST).hasAnyRole(Role.ADMIN.name(), Role.ARCHIVIST.name())
                                .requestMatchers(HttpMethod.PATCH, "/profile/password").authenticated()
                                .requestMatchers(HttpMethod.PATCH).hasAnyRole(Role.ADMIN.name(), Role.ARCHIVIST.name())
                                .requestMatchers(HttpMethod.DELETE).hasAnyRole(Role.ADMIN.name(), Role.ARCHIVIST.name())
                                .requestMatchers(HttpMethod.GET, "*/edit/**", "*/add", "/tools/**").hasAnyRole(Role.ADMIN.name(), Role.ARCHIVIST.name())
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
