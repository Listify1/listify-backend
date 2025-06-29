package com.listify.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configures the application's Cross-Origin Resource Sharing (CORS) settings.
 * <p>
 * This class implements {@link WebMvcConfigurer} to provide a global CORS configuration,
 * allowing web clients from different origins to interact with the API endpoints.
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    /**
     * Defines the global CORS rules for all endpoints in the application.
     * <p>
     * This configuration applies to all paths ({@code "/**"}) and is set up to be
     * broadly permissive for development purposes. For production environments,
     * it is recommended to restrict {@code allowedOriginPatterns} to specific, trusted domains.
     *
     * @param registry the {@link CorsRegistry} to which the CORS mappings are added.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // Allows requests from any origin. For production, replace "*" with specific client URLs.
                .allowedOriginPatterns("*")
                // Allows all standard HTTP methods (GET, POST, PUT, DELETE, etc.).
                .allowedMethods("*")
                // Allows all headers in the request.
                .allowedHeaders("*")
                // Disallows sending credentials (like cookies, authorization headers) with cross-origin requests.
                // Set to true if your frontend needs to send credentials.
                .allowCredentials(false);
    }
}