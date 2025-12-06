package com.fc.authservice.controller;

import com.fc.authservice.dto.OtpVerificationRequest;
import com.fc.authservice.dto.SocialLoginRequest;
import com.fc.authservice.dto.UsersDTO;
import com.fc.authservice.service.AuthService;
import com.fc.authservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for handling authentication and user account related operations.
 * This includes traditional login, social login (Google/Facebook), token validation,
 * OTP-based account verification, password reset, and setting a new password.
 * The controller delegates all business logic to {@link UserService} and {@link AuthService},
 * and is responsible for mapping HTTP requests to appropriate service calls and
 * translating responses into HTTP status codes.
 *
 * @author Romy Rose Jimmy
 * @since 2025
 */
@Slf4j
@RestController
@Tag(name = "Users", description = "API for managing users")
public class AuthController {

    @Autowired
    private UserService usersManagementService;

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    /**
     * Authenticates a user using email/password and returns a token response.
     *
     * @param req the user login request containing credentials
     * @return a response containing user details and authentication token
     */
    @PostMapping("/auth/login")
    @Operation(summary = "Generate token on user login")
    public ResponseEntity<UsersDTO> login(@RequestBody UsersDTO req){
        log.info("Login request received");
        UsersDTO response = userService.login(req);
        log.info("Login response generated with statusCode={}", response.getStatusCode());
        return ResponseEntity.ok(response);
    }

    /**
     * Handles login or registration via social providers such as Google or Facebook.
     *
     * @param request the social login request containing provider details and user info
     * @return a response containing user details and authentication token
     */
    @PostMapping("/auth/social-login")
    @Operation(summary = "Login or register user via social provider (Google/Facebook)")
    public ResponseEntity<UsersDTO> socialLogin(@RequestBody SocialLoginRequest request) {
        log.info("Social login request received for provider={}", request.getProvider());
        UsersDTO response = userService.socialLogin(request);
        log.info("Social login processed with statusCode={}", response.getStatusCode());
        return ResponseEntity.ok(response);
    }

    /**
     * Validates the provided JWT token. If the token is valid, returns HTTP 200,
     * otherwise returns HTTP 401 Unauthorized.
     *
     * @param authHeader the Authorization header containing the Bearer token
     * @return empty response with 200 OK for valid token or 401 UNAUTHORIZED for invalid token
     */
    @GetMapping("/auth/validate")   @Operation(summary = "Validate Token")
    public ResponseEntity<Void> validateToken(
            @RequestHeader("Authorization") String authHeader) {

        // Authorization: Bearer <token>
        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Token validation failed: missing or invalid Authorization header");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7);

        boolean isValid = authService.validateToken(token);
        if (isValid) {
            log.info("Token validation successful");
            return ResponseEntity.ok().build();
        } else {
            log.warn("Token validation failed: invalid token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Extracts the current user information (userId) from a valid JWT token.
     *
     * @param authHeader the Authorization header containing the Bearer token
     * @return a map containing the userId if extraction is successful or appropriate error status
     */
    @GetMapping("/auth/user")
    @Operation(summary = "Get current user info from token")
    public ResponseEntity<Map<String, String>> getUserFromToken(
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Failed to extract user from token: missing or invalid Authorization header");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            String token = authHeader.substring(7); // Remove "Bearer "
            String userId = authService.extractUserId(token); // or extractUsername()

            log.info("Successfully extracted userId from token");

            Map<String, String> response = new HashMap<>();
            response.put("userId", userId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Unexpected error occurred while extracting user from token", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Verifies a user account using an OTP, and optionally updates the email.
     *
     * @param request the OTP verification request containing current email, OTP,
     *                and optional new email
     * @return the updated user DTO with appropriate status code
     */
    @Operation(summary = "Verify account")
    @PutMapping("/auth/verify-account")
    public ResponseEntity<UsersDTO> verifyAccount(@RequestBody OtpVerificationRequest request) {
        log.info("Account verification request received for email={}", request.getCurrentEmail());
        UsersDTO usersDTO = userService.verifyAccount(
                request.getCurrentEmail(),
                request.getOtp(),
                request.isUpdateEmail(),
                request.getNewEmail()
        );
        log.info("Account verification processed with statusCode={}", usersDTO.getStatusCode());
        return new ResponseEntity<>(usersDTO, HttpStatus.valueOf(usersDTO.getStatusCode()));
    }

    /**
     * Regenerates an OTP for account verification or email update purposes.
     *
     * @param email         the target email address for OTP
     * @param isUpdateEmail flag indicating whether this is for updating the email
     * @param currentEmail  the current email address (optional, used when updating email)
     * @return a response containing status and message indicating OTP generation result
     */
    @Operation(summary = "Generate OTP")
    @PutMapping("/auth/regenerate-otp")
    public ResponseEntity<UsersDTO> regenerateOtp(
            @RequestParam String email,
            @RequestParam boolean isUpdateEmail,
            @RequestParam(required = false) String currentEmail
    ) {
        log.info("Regenerate OTP request received for email={}, isUpdateEmail={}", email, isUpdateEmail);

        UsersDTO response = usersManagementService.regenerateOtp(email, isUpdateEmail, currentEmail);

        if (response.getStatusCode() == 200) {
            log.info("OTP regenerated successfully for email={}", email);
            return ResponseEntity.ok(response);
        } else if (response.getStatusCode() == 409) {
            log.warn("OTP regeneration conflict for email={}", email);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } else {
            log.warn("OTP regeneration failed for email={} with statusCode={}", email, response.getStatusCode());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    /**
     * Initiates the forgot password flow by sending a reset link or OTP
     * to the provided email address.
     *
     * @param email the email address associated with the user account
     * @return a response containing status and message about the forgot password operation
     */
    @Operation(summary = "Forgot password")
    @PutMapping("/auth/forgot-password")
    public ResponseEntity<UsersDTO> forgotPassword(@RequestParam String email) {
        log.info("Forgot password request received for email={}", email);
        UsersDTO response = usersManagementService.forgotPassword(email);
        if(response.getStatusCode() == 500){
            log.error("Forgot password operation failed for email={}", email);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        log.info("Forgot password operation completed for email={} with statusCode={}", email, response.getStatusCode());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    /**
     * Sets a new password for a user identified by email.
     *
     * @param email       the email address of the user
     * @param newPassword the new password to be set
     * @return a response containing operation status and message
     */
    @Operation(summary = "Set password")
    @PutMapping("/auth/set-password")
    public  ResponseEntity<UsersDTO> setPassword(@RequestParam String email, @RequestParam String newPassword){
        log.info("Set password request received for email={}", email);

        UsersDTO response = usersManagementService.setPassword(email, newPassword);

        if(response.getStatusCode() ==500){
            log.error("Set password failed for email={} with internal server error", email);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        else if(response.getStatusCode() == 404){
            log.warn("Set password failed: user not found for email={}", email);
            return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
        }
        log.info("Set password completed successfully for email={}", email);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    
}
