package com.fc.authservice.exception;

import java.io.Serial;

/**
 * Custom exception representing a 400 Bad Request error.
 * This exception is thrown when the client sends invalid,
 * malformed, or logically incorrect input data.
 * It is typically handled by a global exception handler
 * which converts it into a standardized API error response.
 * Usage example:
 * throw new BadRequestException("Email is invalid");
 *
 * @since 2025
 * author: Romy Rose Jimmy
 */
public class BadRequestException extends RuntimeException {


    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new BadRequestException with the provided message.
     *
     * @param message Detailed error message describing the bad request
     */
    public BadRequestException(String message) {
        super(message);
    }
}