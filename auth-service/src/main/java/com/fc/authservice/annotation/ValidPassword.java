package com.fc.authservice.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom annotation used to validate password strength based on defined rules.
 * This annotation ensures that the password contains:
 * - At least 8 characters
 * - At least one digit
 * - At least one lowercase letter
 * - At least one uppercase letter
 * - At least one special character
 * - No whitespace characters
 * It is applied on fields in DTOs or entities to enforce strong password policies.
 *
 * @author Romy Rose Jimmy
 * @since 2025
 */
@Documented
@Constraint(validatedBy = PasswordConstraintValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPassword {

    /**
     * Error message returned when the password fails validation.
     */
    String message() default "Password must be at least 8 characters and contain a digit, a lower-case, an upper-case letter, and a special character";

    /**
     * Used to specify validation groups (optional).
     */
    Class<?>[] groups() default {};

    /**
     * Used to carry additional metadata (optional).
     */
    Class<? extends Payload>[] payload() default {};
}
