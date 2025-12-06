package com.fc.apigateway.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Handles JWT validation related exceptions for API Gateway.
 *
 * @author Romy Rose Jimmy
 * @since 2025
 */

@Slf4j
@RestControllerAdvice
public class JwtValidationException {
    @ExceptionHandler(WebClientResponseException.Unauthorized.class)
    public Mono<Void> handleUnauthorizedException(ServerWebExchange exchange){
        // Log the unauthorized attempt
        log.error("Unauthorized access attempt. Path: {}",
                exchange.getRequest().getURI().getPath());
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
