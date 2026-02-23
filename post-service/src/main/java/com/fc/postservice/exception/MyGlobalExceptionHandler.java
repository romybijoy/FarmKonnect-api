package com.fc.postservice.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.NoSuchElementException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import java.util.stream.Collectors;
/**
 * Global exception handler for PostService.
 * This class centralizes exception-to-response mappings and ensures that
 * clients receive consistent and safe error messages.
 * Features:
 *  - Handles domain-level exceptions such as IllegalStateException and NoSuchElementException
 *  - Logs errors for debugging while avoiding sensitive information in responses
 *  - Converts backend exceptions to proper HTTP status codes
 */
@Slf4j
@RestControllerAdvice
public class MyGlobalExceptionHandler {

    /**
     * Handles business rule exceptions and missing resources.
     * These are mapped to:
     *  - 409 Conflict → for IllegalStateException (e.g., duplicate report)
     *  - 409 Conflict → for NoSuchElementException (e.g., missing entity)
     * Why 409?
     *  Conflict is appropriate when:
     *   - The request cannot be processed due to the current state of the resource
     *   - A rule violation happens (duplicate or invalid transitions)
     *
     * @param ex the caught exception
     * @return ResponseEntity containing user-safe error message
     */
    @ExceptionHandler({ IllegalStateException.class, NoSuchElementException.class })
    public ResponseEntity<String> handleConflict(RuntimeException ex) {

        // Business level logging
        log.warn("Operation failed: {}", ex.getMessage());

        // Debug stack trace for developers
        if (log.isDebugEnabled()) {
            log.debug("Stack trace:", ex);
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    //Handle validation errors and return a user-friendly message
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {

        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        return ResponseEntity.badRequest().body(errors);
    }
}
