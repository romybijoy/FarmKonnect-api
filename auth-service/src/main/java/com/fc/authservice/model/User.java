package com.fc.authservice.model;


import com.fc.authservice.annotation.ValidPassword;
import com.fc.authservice.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents an application user stored in the "users" table.
 * This entity contains authentication data (email, password, role),
 * profile information (name, image, district), account status details,
 * and optional social login identifiers.
 */
@Setter
@Getter
@Entity
@Table(name="users")
public class User {

    /** Unique identifier for the user */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    /** User's unique email address used for login */
    @Column(unique = true, nullable = false)
    private String email;

    /** User's encrypted password, validated using @ValidPassword */
    @Column(nullable = false)
    @ValidPassword
    private String password;

    /** Username displayed publicly */
    @Column(name = "user_name", nullable = false)
    private String userName;

    /** Optional mobile phone number */
    @Column(name = "mobile_number")
    private String mobileNumber;

    /** User's role (ADMIN / USER) */
    @Enumerated(EnumType.STRING)
    private Role role;

    /** Profile image URL */
    private String image;

    /** User's profile description / bio */
    private String description;

    /** District or region associated with the user */
    private String district;

    /** Reason for temporarily or permanently blocking the user */
    @Column(name = "block_reason")
    private String blockReason;

    /** Timestamp when the user account was created */
    private LocalDateTime createdAt;

    /** Indicates whether the user account is active/enabled */
    private boolean enabled;

    /** OTP used for verification or password reset */
    private String otp;

    /** Timestamp when OTP was generated */
    private LocalDateTime otpGeneratedTime;

    /** Google OAuth user ID, if user registered via Google */
    private String googleId;

    /** Facebook OAuth user ID, if user registered via Facebook */
    private String facebookId;

    /** Avatar image URL provided by social login provider */
    private String avatarUrl;

    /** Flag to indicate whether user registered using social login */
    private boolean socialUser;

}
