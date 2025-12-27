package com.fc.authservice.exception;


import lombok.Getter;

import java.io.Serial;

/**
 * Custom runtime exception used to represent application-specific errors.
 * This exception allows attaching a custom status code along with the message,
 * making it suitable for propagating meaningful error responses through a
 * global exception handler.
 * Key features:
 * - Extends RuntimeException (unchecked)
 * - Supports custom error codes
 * - Can be thrown across service, controller, or validation layers
 * Usage:
 * throw new APIException("User not found", 404);
 *
 * @since 2025
 * @author
 *   Romy Rose Jimmy
 */
@Getter
public class APIException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final int code;

    /**
     * Creates an APIException with both an error message and a custom error code.
     *
     * @param message the error message
     * @param code    application-specific error code
     */
    public APIException(String message, int code) {
        super(message);
        this.code=code;
    }

}
