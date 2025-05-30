package com.fc.authservice.controller;

import com.fc.authservice.dto.UsersDTO;
import com.fc.authservice.service.AuthService;
import com.fc.authservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
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
    public ResponseEntity<UsersDTO> login(@RequestBody UsersDTO req){
        UsersDTO response = userService.login(req);
        if(response.getStatusCode() == 500){
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        else if(response.getStatusCode() == 403){
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Validate Token")
    @GetMapping("/auth/validate")
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


    @PutMapping("/auth/verify-account")
    public ResponseEntity<UsersDTO> verifyAccount(@RequestParam String email,
                                                  @RequestParam String otp) {
        UsersDTO response = usersManagementService.verifyAccount(email, otp);
        if(response.getStatusCode() == 500){
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
    @PutMapping("/auth/regenerate-otp")
    public ResponseEntity<UsersDTO> regenerateOtp(@RequestParam String email) {
        return new ResponseEntity<>(usersManagementService.regenerateOtp(email), HttpStatus.OK);
    }

    @PutMapping("/auth/forgot-password")
    public ResponseEntity<UsersDTO> forgotPassword(@RequestParam String email) {
        UsersDTO response = usersManagementService.forgotPassword(email);
        if(response.getStatusCode() == 500){
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


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
