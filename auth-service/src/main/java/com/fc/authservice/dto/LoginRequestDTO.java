package com.fc.authservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing the login request payload.
 * Contains the user's email and password, both validated
 * using Jakarta Bean Validation annotations.
 * Used in authentication endpoints to validate user input
 * before passing credentials to the AuthService.
 *
 * @author Romy Rose Jimmy
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDTO {

    /** Email of the user attempting to log in. Must be a valid email format. */
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be a valid email address")
    private String email;

    /** Password of the user. Must be at least 8 characters. */
    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;
}
