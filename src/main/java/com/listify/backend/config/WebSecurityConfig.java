package com.listify.backend.config;

import com.listify.backend.security.JwtFilter;
import jakarta.servlet.MultipartConfigElement;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.unit.DataSize;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Main configuration class for web security.
 * <p>
 * This class enables Spring's web security features with {@code @EnableWebSecurity}
 * and defines the primary security filter chain, CORS settings, and other web-related
 * security configurations. It leverages a custom {@link JwtFilter} for handling
 * JWT-based authentication.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurityConfig {

    /**
     * The custom JWT filter that processes the 'Authorization' header for authentication.
     * Injected via constructor by Lombok's {@code @RequiredArgsConstructor}.
     */
    private final JwtFilter jwtFilter;

    /**
     * Creates a {@link CorsConfigurationSource} bean to manage Cross-Origin Resource Sharing.
     * <p>
     * This configuration explicitly allows requests from the local development server
     * and the standard Android emulator address for the host machine's localhost.
     * It is crucial for enabling frontend applications to communicate with the backend API.
     *
     * @return a configured {@link UrlBasedCorsConfigurationSource} instance.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:8081", "http://10.0.2.2:8081"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Configures the handling of multipart requests, such as file uploads.
     * <p>
     * This bean is essential for allowing Spring to correctly parse 'multipart/form-data'
     * requests. Without it, such requests might be rejected before they reach the
     * controller, even if the endpoint is correctly configured.
     *
     * @return a {@link MultipartConfigElement} with defined size limits.
     */
    @Bean
    public MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        // Sets the maximum size for a single uploaded file (e.g., 10 MB).
        factory.setMaxFileSize(DataSize.ofMegabytes(10));
        // Sets the maximum size for the entire multipart request (e.g., 10 MB).
        factory.setMaxRequestSize(DataSize.ofMegabytes(10));
        return factory.createMultipartConfig();
    }

    /**
     * Defines the security filter chain that applies security rules to incoming HTTP requests.
     *
     * @param http the {@link HttpSecurity} object to configure.
     * @return the configured {@link SecurityFilterChain}.
     * @throws Exception if an error occurs during the configuration.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Apply the CORS configuration defined in the corsConfigurationSource bean.
                .cors(withDefaults())
                // Disable CSRF protection, as it's not needed for stateless APIs that use tokens for auth.
                .csrf(csrf -> csrf.disable())
                // Configure session management to be STATELESS. The server will not create or use HTTP sessions.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Define authorization rules for HTTP requests.
                .authorizeHttpRequests(auth -> auth
                        // Allow all CORS pre-flight OPTIONS requests.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Publicly accessible endpoints for authentication, WebSocket connections, and suggestions.
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/users/*/group").permitAll()
                        .requestMatchers("/api/payments/test").permitAll()
                        .requestMatchers("/api/payments/**").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        .requestMatchers("/api/suggestions/**").permitAll()
                        // Endpoints that require authentication.
                        .requestMatchers("/api/storage/upload").authenticated()
                        // All other requests that are not explicitly permitted must be authenticated.
                        .anyRequest().authenticated()
                )
                // Add the custom JwtFilter before the standard UsernamePasswordAuthenticationFilter.
                // This ensures our token-based authentication is processed for each request.
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}