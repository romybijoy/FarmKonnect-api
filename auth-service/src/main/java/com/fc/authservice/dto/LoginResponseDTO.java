package com.fc.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Response DTO returned after a successful login attempt.
 * Contains the JWT token generated for the authenticated user.
 * This token is used for subsequent authorization of API requests.
 *
 * @author Romy Rose Jimmy
 * @since 2025
 */
@Getter
@AllArgsConstructor
public class LoginResponseDTO {

    /** The JWT token issued upon successful authentication */
    private final String token;
}
