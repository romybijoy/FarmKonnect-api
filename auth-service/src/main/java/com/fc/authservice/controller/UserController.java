package com.fc.authservice.controller;

import com.fc.authservice.config.AppConstants;
import com.fc.authservice.dto.*;
import com.fc.authservice.enums.Role;
import com.fc.authservice.model.User;
import com.fc.authservice.repository.UserRepository;
import com.fc.authservice.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@Tag(name = "Users", description = "API for managing users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserService usersManagementService;
    @Autowired
    private UserRepository userRepository;

    @Operation(summary = "Registration")
    @PostMapping("/register")
    public ResponseEntity<UsersDTO> register(@Valid @RequestBody UsersRequest reg) {
        logger.info("Registration initiated | Email={}", reg.getEmail());
        UsersDTO response = usersManagementService.register(reg);
        logger.info("Registration completed | Email={} | StatusCode={}", reg.getEmail(), response.getStatusCode());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get all users")
    @GetMapping("/get-all-users")
    public ResponseEntity<UsersResponse> getAllUsers(@RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
                                                     @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
                                                     @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_USERS_BY) String sortBy,
                                                     @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR) String sortOrder,
                                                     @RequestParam(name = "enabled", defaultValue = AppConstants.STATUS) boolean enabled) {
        logger.debug("Fetching users | Page={} | Size={} | SortBy={} | Order={} | Enabled={}",
                pageNumber, pageSize, sortBy, sortOrder, enabled);

        UsersResponse response = usersManagementService.getAllUsers(pageNumber, pageSize, sortBy, sortOrder, enabled);
        logger.info("Users retrieved successfully | Count={} | Page={}",
                response != null ? response.getContent().size() : 0, pageNumber);

        return new ResponseEntity<>(response, HttpStatus.FOUND);
    }

    @Operation(summary = "Get all users with filtering")
    @GetMapping("/get-all-users/keyword/{keyword}")
    public ResponseEntity<UsersResponse> getUsersByKeyword(@PathVariable String keyword,
                                                           @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
                                                           @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
                                                           @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_USERS_BY) String sortBy,
                                                           @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR) String sortOrder) {
        logger.debug("Searching users | Keyword={} | Page={} | Size={}", keyword, pageNumber, pageSize);

        UsersResponse response = usersManagementService.searchUsersByKeyword(keyword, pageNumber, pageSize, sortBy, sortOrder);
        logger.info("Search completed | Keyword={} | Results={}", keyword,
                response != null ? response.getContent().size() : 0);

        return new ResponseEntity<>(response, HttpStatus.FOUND);
    }

    @Operation(summary = "Get user by ID")
    @GetMapping("/get-users/{userId}")
    public ResponseEntity<UsersDTO> getUserByID(@PathVariable UUID userId) {
        logger.debug("Fetching user details | UserId={}", userId);
        UsersDTO response = usersManagementService.getUsersById(userId);

        if (response.getStatusCode() == 500) {
            logger.error("Failed to fetch user | UserId={} | StatusCode=500", userId);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        logger.info("User details retrieved successfully | UserId={}", userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get user by role")
    @GetMapping("/get-users/find/{role}")
    public ResponseEntity<UsersDTO> getUserByRole(@PathVariable Role role) {
        logger.debug("Fetching user by role | Role={}", role);
        UsersDTO response = usersManagementService.getUsersByRole(role);

        if (response.getStatusCode() == 500) {
            logger.error("Failed to fetch user by role | Role={} | StatusCode=500", role);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        logger.info("User fetched successfully by role | Role={}", role);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Update user details")
    @PutMapping("/update/{userId}")
    public ResponseEntity<UsersDTO> updateUser(@PathVariable UUID userId, @RequestBody User req) {
        logger.info("User update initiated | UserId={} | Email={}", userId, req.getEmail());
        UsersDTO updatedUser = usersManagementService.updateUser(userId, req);
        logger.info("User update completed | UserId={} | StatusCode={}", userId, updatedUser.getStatusCode());
        return ResponseEntity.ok(updatedUser);
    }

    @Operation(summary = "Get logged in user details")
    @GetMapping("/get-profile/{email}")
    public ResponseEntity<UserDTO> getMyProfile(@PathVariable String email) {
        logger.debug("Fetching profile | Email={}", email);
        UserDTO userDTO = usersManagementService.getMyInfo(email);
        logger.info("Profile fetched successfully | Email={}", email);
        return ResponseEntity.ok(userDTO);
    }

    @Operation(summary = "Delete user")
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<UsersDTO> deleteUser(@PathVariable UUID userId) {
        logger.warn("User deletion requested | UserId={}", userId);
        UsersDTO response = usersManagementService.deleteUser(userId);
        logger.info("User deleted successfully | UserId={}", userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Block user")
    @PutMapping("/block/{userId}")
    public ResponseEntity<UsersDTO> blockUser(@PathVariable UUID userId, @RequestBody UsersDTO req) {
        logger.warn("User block initiated | UserId={}", userId);
        UsersDTO response = usersManagementService.blockUser(userId, req);

        if (response.getStatusCode() == 500) {
            logger.error("User block failed | UserId={} | StatusCode=500", userId);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        logger.info("User block action completed | UserId={} | Blocked={}", userId,
               response.getBlockReason());
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get block status")
    @GetMapping("/auth/{email}/block-status")
    public ResponseEntity<BlockStatusResponse> getBlockStatus(@PathVariable String email) {
        logger.debug("Checking block status | Email={}", email);
        BlockStatusResponse response = usersManagementService.checkBlockStatus(email);
        logger.info("Block status retrieved | Email={} | Blocked={}", email, response.isBlocked());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/multiple")
    public ResponseEntity<List<FollowUserDTO>> getUsersByIds(@RequestBody(required = false) UserIdsRequest request) {
        if (request == null || request.getIds() == null || request.getIds().isEmpty()) {
            logger.warn("Invalid request received for /multiple | Reason=Empty or null user IDs");
            return ResponseEntity.badRequest().build();
        }

        logger.debug("Fetching multiple users | Count={}", request.getIds().size());

        List<FollowUserDTO> users = usersManagementService.getUsersByIds(request);
        logger.info("Fetched multiple users successfully | Retrieved={}",
                users != null ? users.size() : 0);

        return ResponseEntity.ok(users);
    }


    @GetMapping("/validate-email")
    public ResponseEntity<Map<String, Object>> validateEmail(@RequestParam String email) {
        logger.debug("Validating email availability | Email={}", email);
        boolean exists = userRepository.existsByEmailIgnoreCase(email);
        logger.info("Email validation result | Email={} | Exists={}", email, exists);

        return ResponseEntity.ok(
                Map.of(
                        "valid", !exists,
                        "message", exists ? "Email already registered" : "Email is available"
                )
        );
    }
}
