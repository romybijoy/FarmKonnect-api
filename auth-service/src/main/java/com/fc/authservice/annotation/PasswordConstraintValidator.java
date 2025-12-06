package com.fc.authservice.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.extern.slf4j.Slf4j;

/**
 * Validator for checking password strength based on custom @ValidPassword annotation.
 * Ensures the password contains at least one digit, one lowercase letter,
 * one uppercase letter, one special character, no whitespace, and a minimum length of 8.
 * This validator is used across authentication-related operations to enforce
 * strong password policies for user accounts.
 *
 * @author Romy Rose Jimmy
 * @since 2025
 */
@Slf4j
public class PasswordConstraintValidator implements ConstraintValidator<ValidPassword, String> {

    private static final String DEFAULT_VALIDATION_PATTERN =
            "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$";

    /**
     * Validates the incoming password against a strong regex rule.
     *
     * @param password the password value submitted by the user
     * @param context validation context for reporting violations
     * @return true if password meets all required conditions, false otherwise
     */
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null || password.isBlank()) {
            log.error("Password validation failed: password is null or blank");
            return false;
        }
        boolean isValid = password.matches(DEFAULT_VALIDATION_PATTERN);

        if (!isValid) {
            log.error("Password validation failed: password does not meet complexity requirements");
        }

        return isValid;
    }
}
