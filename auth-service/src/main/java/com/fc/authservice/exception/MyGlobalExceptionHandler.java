package com.fc.authservice.exception;
import java.time.LocalDateTime;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


import org.springframework.web.context.request.WebRequest;

/**
 * Global exception handler for the Auth Service.
 * This class centralizes the handling of all exceptions thrown by controllers
 * and services, and converts them into consistent HTTP responses.
 * It handles:
 * - Domain-specific exceptions (UserNotFoundException, PasswordMismatchException, etc.)
 * - Validation errors (MethodArgumentNotValidException)
 * - Authentication errors
 * - Generic unhandled exceptions
 * All unexpected or error conditions are logged so they can be inspected
 * using the configured logging system (Logback -> error.log).
 *
 * @author Romy Rose Jimmy
 * @since 2025
 */
@Slf4j
@RestControllerAdvice
public class MyGlobalExceptionHandler {

    /**
     * Handles cases where a requested user is not found.
     *
     * @param ex the thrown UserNotFoundException
     * @return a NOT_FOUND (404) response with error details
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        log.error("User not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, ex.getMessage()));
    }

    /**
     * Handles password mismatch scenarios, typically during login or password change.
     *
     * @param ex the thrown PasswordMismatchException
     * @return an UNAUTHORIZED (401) response with error details
     */
    @ExceptionHandler(PasswordMismatchException.class)
    public ResponseEntity<ErrorResponse> handlePasswordMismatch(PasswordMismatchException ex) {
        log.warn("Password mismatch: {}", ex.getMessage());
        ErrorResponse error = new ErrorResponse(HttpStatus.UNAUTHORIZED.value(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
    }

    /**
     * Handles invalid or malformed client requests.
     *
     * @param ex the thrown BadRequestException
     * @return a BAD_REQUEST (400) response with error details
     */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
        log.warn("Bad request: {}", ex.getMessage());
        return new ResponseEntity<>(new ErrorResponse(400, ex.getMessage()), HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles cases where the user is not allowed to access a resource.
     *
     * @param ex the thrown ForbiddenException
     * @return a FORBIDDEN (403) response with error details
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex) {
        log.warn("Forbidden request: {}", ex.getMessage());
        return new ResponseEntity<>(new ErrorResponse(403, ex.getMessage()), HttpStatus.FORBIDDEN);
    }

    /**
     * Handles custom APIException which carries an error code.
     * The code is mapped to appropriate HTTP status codes.
     *
     * @param e the thrown APIException
     * @return a response with mapped HTTP status and error body
     */
    @ExceptionHandler(APIException.class)
    public ResponseEntity<ErrorResponse> myAPIException(APIException e) {
        String message = e.getMessage();
        int code = e.getCode();

        log.error("APIException occurred with code={} and message={}", code, message);
        ErrorResponse res = new ErrorResponse(code, message, LocalDateTime.now());
        HttpStatus status;
        switch (code) {
            case 409 -> status = HttpStatus.CONFLICT;
            case 404 -> status = HttpStatus.NOT_FOUND;
            default -> status = HttpStatus.BAD_REQUEST;
        }
        return new ResponseEntity<>(res, status);
    }

    /**
     * Handles Spring Security authentication exceptions.
     *
     * @param e the thrown AuthenticationException
     * @return a BAD_REQUEST (400) response containing the exception message
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> myAuthenticationException(AuthenticationException e) {
        log.warn("AuthenticationException: {}", e.getMessage());
        String res = e.getMessage();

        return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles custom UserException for user-related errors.
     *
     * @param ue  the thrown UserException
     * @param req the current web request
     * @return a BAD_REQUEST (400) response with detailed error info
     */
    @ExceptionHandler(UserException.class)
    public ResponseEntity<ErrorDetails> userExceptionHandler(UserException ue, WebRequest req){
        log.error("UserException: {} | Path={}", ue.getMessage(), req.getDescription(false));

        ErrorDetails err= new ErrorDetails(ue.getMessage(),req.getDescription(false), LocalDateTime.now());

        return new ResponseEntity<>(err, HttpStatus.BAD_REQUEST);

    }

    /**
     * Handles all uncaught exceptions in the application.
     * Acts as a safety net for unexpected errors.
     *
     * @param ex the thrown generic Exception
     * @return an INTERNAL_SERVER_ERROR (500) response
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex) {
        log.error("Unhandled exception occurred", ex);
        return new ResponseEntity<>(new ErrorResponse(500, "Internal Server Error"), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * Handles validation errors thrown when method arguments
     * annotated with validation constraints fail (e.g. @Valid DTOs).
     *
     * @param ex the thrown MethodArgumentNotValidException
     * @return a BAD_REQUEST (400) response with first validation error message
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .orElse("Validation error");

        log.warn("Validation failed: {}", errorMessage);
        ErrorResponse res = new ErrorResponse(400, errorMessage, LocalDateTime.now());
        return new ResponseEntity<>(res, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles errors that occur while sending emails (e.g., SMTP failure).
     *
     * @param ex  the thrown EmailSendException
     * @param req the current web request
     * @return an INTERNAL_SERVER_ERROR (500) response with detailed error info
     */
    @ExceptionHandler(EmailSendException.class)
    public ResponseEntity<ErrorDetails> handleEmailSendException(EmailSendException ex, WebRequest req) {
        log.error("EmailSendException: {} | Path={}", ex.getMessage(), req.getDescription(false));

        ErrorDetails error = new ErrorDetails(
                ex.getMessage(),
                req.getDescription(false)
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
