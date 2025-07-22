package com.fc.chatservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class AuthServiceClient {

    @Value("${auth-service.url}")
    private String authServiceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean validateToken(String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Void> response = restTemplate.exchange(
                    authServiceUrl + "/auth/validate",
                    HttpMethod.GET,
                    entity,
                    Void.class
            );

            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String getUserId(String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(token);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    authServiceUrl + "/auth/user",
                    HttpMethod.GET,
                    entity,
                    Map.class
            );

            Map body = response.getBody();
            if (body != null && body.containsKey("userId")) {
                return (String) body.get("userId");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
