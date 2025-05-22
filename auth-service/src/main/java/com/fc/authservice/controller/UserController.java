package com.fc.authservice.controller;

import com.fc.authservice.config.AppConstants;
import com.fc.authservice.dto.UsersDTO;
import com.fc.authservice.dto.UsersRequest;
import com.fc.authservice.dto.UsersResponse;
import com.fc.authservice.enums.Role;
import com.fc.authservice.model.User;
import com.fc.authservice.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class UserController {

    @Autowired
    private UserService usersManagementService;

    @PostMapping("/register")
    public ResponseEntity<UsersDTO> register(@Valid @RequestBody UsersRequest reg){
        UsersDTO response = usersManagementService.register(reg);

        if(response.getStatusCode() == 500){
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/admin/login")
    public ResponseEntity<UsersDTO> login(@RequestBody UsersDTO req){
        UsersDTO response = usersManagementService.login(req);
        if(response.getStatusCode() == 500){
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        else if(response.getStatusCode() == 403){
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/get-all-users")
    public ResponseEntity<UsersResponse> getAllUsers(@RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
                                                     @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
                                                     @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_USERS_BY, required = false) String sortBy,
                                                     @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder,
                                                     @RequestParam(name = "enabled", defaultValue = AppConstants.STATUS, required = false) boolean enabled){

        UsersResponse usersResponse = usersManagementService.getAllUsers(pageNumber, pageSize, sortBy, sortOrder,enabled);

        return new ResponseEntity<>(usersResponse, HttpStatus.FOUND);
    }


    @GetMapping("/admin/get-all-users/keyword/{keyword}")
    public ResponseEntity<UsersResponse> getUsersByKeyword(@PathVariable String keyword,
                                                           @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER, required = false) Integer pageNumber,
                                                           @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE, required = false) Integer pageSize,
                                                           @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_USERS_BY, required = false) String sortBy,
                                                           @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR, required = false) String sortOrder) {

        UsersResponse usersResponse = usersManagementService.searchUsersByKeyword(keyword, pageNumber, pageSize, sortBy,
                sortOrder);

        return new ResponseEntity<>(usersResponse, HttpStatus.FOUND);
    }


    @GetMapping("/admin/get-users/{userId}")
    public ResponseEntity<UsersDTO> getUserByID(@PathVariable UUID userId){
        UsersDTO response = usersManagementService.getUsersById(userId);
        System.out.println(response);
        if(response.getStatusCode() == 500){
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(response);

    }

    @GetMapping("/admin/get-users/find/{role}")
    public ResponseEntity<UsersDTO> getUserByRole(@PathVariable Role role){
        UsersDTO response = usersManagementService.getUsersByRole(role);
        System.out.println(response);
        if(response.getStatusCode() == 500){
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(response);

    }

    @PutMapping("/admin/update/{userId}")
    public ResponseEntity<UsersDTO> updateUser(@PathVariable UUID userId, @RequestBody User reqres){
        UsersDTO response = usersManagementService.updateUser(userId, reqres);
        if(response.getStatusCode() == 500){
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(response);
    }

//    @GetMapping("/adminuser/get-profile")
//    public ResponseEntity<UsersDTO> getMyProfile(){
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        String email = authentication.getName();
//        UsersDTO response = usersManagementService.getMyInfo(email);
//        return  ResponseEntity.status(response.getStatusCode()).body(response);
//    }

    @DeleteMapping("/admin/delete/{userId}")
    public ResponseEntity<UsersDTO> deleteUSer(@PathVariable UUID userId){
        return ResponseEntity.ok(usersManagementService.deleteUser(userId));
    }

    @PutMapping("/admin/block/{userId}")
    public ResponseEntity<UsersDTO> blockUser(@PathVariable UUID userId, @RequestBody UsersDTO req){
        UsersDTO response = usersManagementService.blockUser(userId, req);
        if(response.getStatusCode() == 500){
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        return ResponseEntity.ok(response);
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
