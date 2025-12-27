package com.fc.stories_service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Stories Service application.
 * Includes startup logs and endpoint mapping output for easier debugging.
 */
@SpringBootApplication
@Slf4j
public class StoriesServiceApplication {

    public static void main(String[] args) {

        log.info("Starting Stories Service...");
        SpringApplication.run(StoriesServiceApplication.class, args);
        log.info("Stories Service started successfully.");
    }

}
