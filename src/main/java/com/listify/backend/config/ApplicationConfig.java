package com.listify.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Central configuration for application-wide beans.
 * <p>
 * This class is used to define beans that are shared across the application,
 * which helps in preventing circular dependency issues.
 */
@Configuration
public class ApplicationConfig {

    /**
     * Provides a singleton {@link PasswordEncoder} bean that can be used
     * throughout the application to encode and verify passwords.
     *
     * @return an instance of {@link BCryptPasswordEncoder}.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}