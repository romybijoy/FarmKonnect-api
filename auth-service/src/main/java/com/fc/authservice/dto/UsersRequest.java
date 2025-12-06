package com.fc.authservice.dto;

import com.fc.authservice.annotation.ValidPassword;
import com.fc.authservice.enums.Role;
import com.fc.authservice.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Request DTO used for creating or updating user information.
 * This object contains multiple user-related fields and is used
 * across registration, admin operations, and internal user updates.
 * IMPORTANT:
 * Fields such as role, enabled, blockReason, ourUsers, and ourUsersList
 * are system-controlled values. They should be used carefully based on the
 * context in which this DTO is consumed.
 * Validation is applied on essential user-input fields like email,
 * password, and mobile number to ensure data integrity.
 *
 * @author Romy Rose Jimmy
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsersRequest {

    /** Full name of the user */
    private String name;

    /** City where the user resides */
    private String city;

    /** User email address used as the login username */
    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    private String email;

    /** User's mobile number (exactly 10 digits, numeric only) */
    @NotBlank(message = "Mobile number is required")
    @Size(min = 10, max = 10, message = "Mobile Number must be exactly 10 digits long")
    @Pattern(regexp = "^\\d{10}$", message = "Mobile Number must contain only Numbers")
    private String mobileNumber;

    /** Password chosen by the user — validated with strong password rules */
    @NotBlank(message = "Password is required")
    @ValidPassword
    private String password;

    /** Optional profile description or bio */
    private String description;

    /** District where the user lives */
    private String district;

    /** Optional profile image URL */
    private String image;

    /** Role assigned to the user (e.g., USER, ADMIN) */
    private Role role;
    /** Reason for blocking the user, if applicable */
    private String blockReason;

    /** Indicates whether the user account is active */
    private boolean enabled;

    /** Nested user object (used in internal operations or mappings) */
    private User ourUsers;

    /** List of nested user objects (used in admin bulk operations) */
    private List<User> ourUsersList;
}