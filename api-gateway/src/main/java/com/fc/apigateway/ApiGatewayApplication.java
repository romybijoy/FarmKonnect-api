package com.fc.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the API Gateway application.
 * Bootstraps the Spring Boot WebFlux gateway service.
 *
 * @author Romy Rose Jimmy
 * @since 2025
 */

@SpringBootApplication
public class ApiGatewayApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApiGatewayApplication.class, args);
	}

}
