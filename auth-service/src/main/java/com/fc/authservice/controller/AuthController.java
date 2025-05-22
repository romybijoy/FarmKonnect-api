package com.fc.authservice.controller;

import com.fc.authservice.dto.UsersDTO;
import com.fc.authservice.service.AuthService;
import com.fc.authservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

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


    @PostMapping("/login")
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
    @GetMapping("/validate")
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




}
