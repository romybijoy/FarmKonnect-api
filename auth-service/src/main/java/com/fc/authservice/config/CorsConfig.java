package com.fc.authservice.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * CORS configuration for the Auth Service.
 * This class can be extended to define custom CORS rules
 * for handling cross-origin requests from frontend applications.
 * Currently, returns a basic WebMvcConfigurer instance without
 * specific overrides. The configuration can be updated as needed
 * when integrating with real clients.
 *
 * @author Romy Rose Jimmy
 * @since 2025
 */
@Configuration
public class CorsConfig {

    /**
     * Provides a WebMvcConfigurer bean that can be customized to enable
     * or modify Cross-Origin Resource Sharing (CORS) settings.
     *
     * @return a basic WebMvcConfigurer instance (currently without overrides)
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
          // Add CORS configuration overrides here when needed.
        };
    }
}
