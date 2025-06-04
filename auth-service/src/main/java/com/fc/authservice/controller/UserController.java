package com.fc.authservice.controller;

import com.fc.authservice.config.AppConstants;
import com.fc.authservice.dto.UsersDTO;
import com.fc.authservice.dto.UsersRequest;
import com.fc.authservice.dto.UsersResponse;
import com.fc.authservice.enums.Role;
import com.fc.authservice.model.User;
import com.fc.authservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@Tag(name = "Users", description = "API for managing users")
public class UserController {

    @Autowired
    private UserService usersManagementService;

    @Operation(summary = "Registration")
    @PostMapping("/register")
    public ResponseEntity<UsersDTO> register(@Valid @RequestBody UsersRequest reg){
        UsersDTO response = usersManagementService.register(reg);

        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all users")
    @GetMapping("/get-all-users")
    public ResponseEntity<UsersResponse> getAllUsers(@RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
                                                     @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
                                                     @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_USERS_BY, required = false) String sortBy,
                                                     @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder,
                                                     @RequestParam(name = "enabled", defaultValue = AppConstants.STATUS, required = false) boolean enabled){

        UsersResponse usersResponse = usersManagementService.getAllUsers(pageNumber, pageSize, sortBy, sortOrder,enabled);

        return new ResponseEntity<>(usersResponse, HttpStatus.FOUND);
    }

    @Operation(summary = "Get all users with filtering")
    @GetMapping("/get-all-users/keyword/{keyword}")
    public ResponseEntity<UsersResponse> getUsersByKeyword(@PathVariable String keyword,
                                                           @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
                                                           @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
                                                           @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_USERS_BY, required = false) String sortBy,
                                                           @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder) {

        UsersResponse usersResponse = usersManagementService.searchUsersByKeyword(keyword, pageNumber, pageSize, sortBy,
                sortOrder);

        return new ResponseEntity<>(usersResponse, HttpStatus.FOUND);
    }

    @Operation(summary = "Get uer by ID")
    @GetMapping("/get-users/{userId}")
    public ResponseEntity<UsersDTO> getUserByID(@PathVariable UUID userId){
        UsersDTO response = usersManagementService.getUsersById(userId);
        System.out.println(response);
        if(response.getStatusCode() == 500){
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(response);

    }

    @Operation(summary = "Get user by role")
    @GetMapping("/get-users/find/{role}")
    public ResponseEntity<UsersDTO> getUserByRole(@PathVariable Role role){
        UsersDTO response = usersManagementService.getUsersByRole(role);
        System.out.println(response);
        if(response.getStatusCode() == 500){
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(response);

    }

    @Operation(summary = "Update user details")
    @PutMapping("/update/{userId}")
    public ResponseEntity<UsersDTO> updateUser(@PathVariable UUID userId, @RequestBody User reqres){
        UsersDTO response = usersManagementService.updateUser(userId, reqres);
        if(response.getStatusCode() == 500){
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get logged in user details")
    @GetMapping("/get-profile")
    public ResponseEntity<UsersDTO> getMyProfile(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        UsersDTO response = usersManagementService.getMyInfo(email);
        return  ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @Operation(summary = "Delete user")
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<UsersDTO> deleteUSer(@PathVariable UUID userId){
        return ResponseEntity.ok(usersManagementService.deleteUser(userId));
    }

    @Operation(summary = "Block user")
    @PutMapping("/block/{userId}")
    public ResponseEntity<UsersDTO> blockUser(@PathVariable UUID userId, @RequestBody UsersDTO req){
        UsersDTO response = usersManagementService.blockUser(userId, req);
        if(response.getStatusCode() == 500){
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/auth/check-status")
    public ResponseEntity<?> checkUserStatus(Authentication auth) {
        User user = usersManagementService.getUserByUsername(auth.getName());
        return ResponseEntity.ok(Map.of("blocked", user.isEnabled()));
    }

}
