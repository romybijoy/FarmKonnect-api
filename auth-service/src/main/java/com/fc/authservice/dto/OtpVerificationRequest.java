package com.fc.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO used for verifying a user's account through OTP.
 * Supports both normal OTP verification and email update scenarios.
 * Fields:
 * - currentEmail: The user's existing registered email
 * - otp: The one-time password sent to the user
 * - isUpdateEmail: Flag to indicate whether the user wants to update their email
 * - newEmail: The new email address (only used when updating email)
 * Used in account verification and email change flows.
 *
 * @author Romy Rose Jimmy
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerificationRequest {

    /** The user's current registered email address */
    private String currentEmail;

    /** The OTP entered by the user for verification */
    private String otp;

    /** Indicates whether this OTP verification is part of an email update */
    private boolean isUpdateEmail;

    /** The new email to replace the existing one (only if isUpdateEmail = true) */
    private String newEmail;
}
