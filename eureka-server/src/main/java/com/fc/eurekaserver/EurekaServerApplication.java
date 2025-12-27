package com.fc.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * Main entry point for the Eureka Server.
 *
 * This application starts a Eureka service registry that allows
 * microservices to register themselves and discover other services.
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    /**
     * Bootstraps the Eureka Server application.
     *
     * @param args program arguments
     */
    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }

}
