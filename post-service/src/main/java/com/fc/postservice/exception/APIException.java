package com.fc.postservice.exception;


import lombok.Getter;

import java.io.Serial;

/**
 * Custom runtime exception used across the PostService to represent
 * API-related errors with an optional application-specific status code.
 * This exception is typically handled in a global exception handler
 * (e.g., using {@code @ControllerAdvice}) to translate backend errors
 * into meaningful HTTP responses.
 * Example usages:
 *  - Throwing business rule violations
 *  - Returning structured error responses to clients
 *  - Adding custom error codes for admin dashboards or clients
 */
@Getter
public class APIException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Optional application-specific error code.
     * Defaults to 0 when not provided.
     * This is NOT an HTTP status code, but an internal identifier.
     */
    private final int code;

    /**
     * Creates an APIException with a message and a custom application error code.
     *
     * @param message human-readable error description
     * @param code    internal error code for categorizing failures
     */
    public APIException(String message, int code) {
        super(message);
        this.code=code;
    }

}
