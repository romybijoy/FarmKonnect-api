package com.fc.authservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fc.authservice.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO representing basic user profile information.
 * This object is used across the application to expose safe,
 * non-sensitive details about a user such as name, contact info,
 * role, profile image, and account status.
 * Fields are included only if non-null to keep responses clean.
 * Unknown JSON fields are ignored during deserialization.
 * Common usage:
 *  - Profile pages
 *  - Follower/following lists
 *  - Admin user management
 *  - Token-based user info retrieval
 *
 * @author Romy Rose Jimmy
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDTO {

    /** Unique identifier of the user */
    private UUID id;

    /** User's full name */
    private String name;

    /** City where the user resides */
    private String city;

    /** User's registered email address */
    private String email;

    /** User's mobile number */
    private String mobileNumber;

    /** Role assigned to the user (ADMIN, USER, etc.) */
    private Role role;

    /** URL of the user's profile image */
    private String image;

    /** Reason for block, if user account is blocked */
    private String blockReason;

    /** User's description or bio */
    private String description;

    /** District information of the user */
    private String district;

    /** Indicates whether the user account is active/enabled */
    private boolean enabled;
}