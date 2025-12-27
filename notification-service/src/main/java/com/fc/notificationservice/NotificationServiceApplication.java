package com.fc.notificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Notification Service application.
 * This Spring Boot service listens to Kafka follow events,
 * stores notifications in MongoDB, and pushes real-time updates
 * to users via WebSocket (STOMP).
 */
@SpringBootApplication
public class NotificationServiceApplication {

    /**
     * Bootstraps the Notification Service.
     *
     * @param args startup arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }

}
