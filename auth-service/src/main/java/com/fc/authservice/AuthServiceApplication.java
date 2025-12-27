package com.fc.authservice;

import org.modelmapper.ModelMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Main entry point for the Auth Service application.
 * This class initializes the Spring Boot application and defines
 * application-wide beans such as ModelMapper, used for DTO–entity mapping.
 */
@SpringBootApplication
public class AuthServiceApplication {

    /**
     * Configures a ModelMapper bean to be available in the Spring context.
     * ModelMapper is used for converting between DTOs and entities.
     *
     * @return a configured ModelMapper instance
     */
    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    /**
     * Bootstraps the Spring Boot Auth Service application.
     *
     * @param args program arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(AuthServiceApplication.class, args);
    }

}
