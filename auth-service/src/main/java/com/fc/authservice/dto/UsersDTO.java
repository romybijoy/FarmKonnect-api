package com.fc.authservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fc.authservice.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * A comprehensive DTO used for returning user-related responses across various
 * authentication and user management operations. This class acts as a flexible
 * wrapper for responses involving:
 * - Login / Social Login
 * - Registration
 * - OTP Verification
 * - Profile retrieval
 * - Block/Unblock operations
 * - Bulk user fetch results
 * Fields are included only when non-null to keep API responses clean.
 * Unknown JSON fields are safely ignored during deserialization.
 * SECURITY NOTE:
 * The 'password' field is annotated with @JsonIgnore to ensure sensitive
 * information is never sent back to clients.
 * This DTO may include:
 * - Single user details (via ourUsers)
 * - Multiple users (via ourUsersList)
 * - Authentication token and metadata
 *
 * @author Romy Rose Jimmy
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UsersDTO {

    /** HTTP-like status code representing the outcome of the operation */
    private int statusCode;

    /** Error description if the request failed */
    private String error;

    /** JWT token issued on successful authentication */
    private String token;

    /** Unique identifier of the user */
    private UUID userId;

    /** User-friendly message describing the operation result */
    private String message;

    /** JWT Refresh token (optional) */
    private String refreshToken;

    /** Token expiration timestamp (optional) */
    private String expirationTime;

    /** Full name of the user */
    private String name;

    /** City where the user resides */
    private String city;

    /** User's email address */
    private String email;

    /** User's mobile phone number */
    private String mobileNumber;

    /** Password is NEVER returned in API responses for security reasons */
    private String password;

    /** Assigned user role (ADMIN, USER, etc.) */
    private Role role;

    /** URL of the user's profile picture */
    private String image;

    /** Reason for blocking the user (if account is blocked) */
    private String blockReason;

    /** Indicates whether the user account is active/enabled */
    private boolean enabled;

    /** A nested user object for detailed profile responses */
    private UserDTO ourUsers;

    /** List of user profiles (used in search and admin operations) */
    private List<UserDTO> ourUsersList;
}