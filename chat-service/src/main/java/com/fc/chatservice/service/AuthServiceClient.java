package com.fc.chatservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthServiceClient {

    private final RestTemplate restTemplate;
    private final String authServiceUrl;

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceClient.class);

    public AuthServiceClient(RestTemplate restTemplate,
                             @Value("${auth-service.url}") String authServiceUrl) {
        this.restTemplate = restTemplate;
        this.authServiceUrl = authServiceUrl;
    }

    public boolean validateToken(String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    authServiceUrl + "/auth/user",
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );


            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            logger.error("Error validating token with auth service", e);
            return false;
        }
    }

    public UUID getUserId(String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                    authServiceUrl + "/auth/user",
                    HttpMethod.GET,
                    entity,
                    new ParameterizedTypeReference<>() {}
            );

            Map<String, Object> body = response.getBody();
            if (body == null || !body.containsKey("userId")) {
                throw new IllegalStateException("Auth service response missing userId");
            }

            return UUID.fromString(body.get("userId").toString());
        } catch (Exception e) {
            logger.error("Error fetching userId from auth service", e);
            throw new IllegalStateException("Unable to authenticate WebSocket user", e);
        }
    }
}
