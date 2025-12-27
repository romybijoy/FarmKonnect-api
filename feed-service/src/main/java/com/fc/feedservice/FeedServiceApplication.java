package com.fc.feedservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Feed Service application.
 * This Spring Boot application is responsible for generating
 * personalized feeds for users by combining data from gRPC-based
 * follow-service, post-service, and auth-service.
 */
@SpringBootApplication
public class FeedServiceApplication {

    /**
     * Bootstraps the Feed Service application.
     *
     * @param args application startup arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(FeedServiceApplication.class, args);
    }

}
