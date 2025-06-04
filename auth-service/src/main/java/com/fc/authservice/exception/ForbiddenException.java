package com.fc.authservice.exception;

// Forbidden (403)
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
}
