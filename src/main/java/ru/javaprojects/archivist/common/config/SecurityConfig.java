package ru.javaprojects.archivist.common.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
                .deleteCookies("JSESSIONID");
        return http.build();
    }
}
