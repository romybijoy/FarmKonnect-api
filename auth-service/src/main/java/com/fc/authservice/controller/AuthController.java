package com.fc.authservice.controller;

import com.fc.authservice.dto.OtpVerificationRequest;
import com.fc.authservice.dto.UsersDTO;
import com.fc.authservice.service.AuthService;
import com.fc.authservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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

//    @Operation(summary = "Generate token on user login")
//    @PostMapping("/login")
//    public ResponseEntity<LoginResponseDTO> login(
//            @RequestBody LoginRequestDTO loginRequestDTO) {
//
//        Optional<String> tokenOptional = authService.authenticate(loginRequestDTO);
//
//        if (tokenOptional.isEmpty()) {
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        }
//
//        String token = tokenOptional.get();
//        return ResponseEntity.ok(new LoginResponseDTO(token));
//    }


    @PostMapping("/auth/login")
    @Operation(summary = "Generate token on user login")
    public ResponseEntity<UsersDTO> login(@RequestBody UsersDTO req){
        UsersDTO response = userService.login(req);
//        if(response.getToken() == null){
//            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
//        }

//        if(response.getStatusCode() == 500){
//            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//        }
//        else if(response.getStatusCode() == 403){
//            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
//        }

        return ResponseEntity.ok(response);
    }


    @GetMapping("/auth/validate")   @Operation(summary = "Validate Token")
    public ResponseEntity<Void> validateToken(
            @RequestHeader("Authorization") String authHeader) {

        // Authorization: Bearer <token>
        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return authService.validateToken(authHeader.substring(7))
                ? ResponseEntity.ok().build()
                : ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @GetMapping("/auth/user")
    @Operation(summary = "Get current user info from token")
    public ResponseEntity<Map<String, String>> getUserFromToken(
            @RequestHeader("Authorization") String authHeader) {

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            String token = authHeader.substring(7); // Remove "Bearer "
            String userId = authService.extractUserId(token); // or extractUsername()

            Map<String, String> response = new HashMap<>();
            response.put("userId", userId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace(); // Log the error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @Operation(summary = "Verify account")
    @PutMapping("/auth/verify-account")
    public ResponseEntity<UsersDTO> verifyAccount(@RequestBody OtpVerificationRequest request) {
        UsersDTO usersDTO = userService.verifyAccount(
                request.getCurrentEmail(),
                request.getOtp(),
                request.isUpdateEmail(),
                request.getNewEmail()
        );
        return new ResponseEntity<>(usersDTO, HttpStatus.valueOf(usersDTO.getStatusCode()));
    }

    @Operation(summary = "Generate OTP")
    @PutMapping("/auth/regenerate-otp")
    public ResponseEntity<UsersDTO> regenerateOtp(
            @RequestParam String email,
            @RequestParam boolean isUpdateEmail,
            @RequestParam(required = false) String currentEmail
    ) {
        UsersDTO response = usersManagementService.regenerateOtp(email, isUpdateEmail, currentEmail);

        if (response.getStatusCode() == 200) {
            return ResponseEntity.ok(response);
        } else if (response.getStatusCode() == 409) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }


    @Operation(summary = "Forgot password")
    @PutMapping("/auth/forgot-password")
    public ResponseEntity<UsersDTO> forgotPassword(@RequestParam String email) {
        UsersDTO response = usersManagementService.forgotPassword(email);
        if(response.getStatusCode() == 500){
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Set password")
    @PutMapping("/auth/set-password")
    public  ResponseEntity<UsersDTO> setPassword(@RequestParam String email, @RequestParam String newPassword){
        UsersDTO response = usersManagementService.setPassword(email, newPassword);

        if(response.getStatusCode() ==500){
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        else if(response.getStatusCode() == 404){
            return new ResponseEntity<>(response, HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


}
