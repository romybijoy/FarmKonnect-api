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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for managing user accounts in the Auth Service.
 * Provides APIs for registration, user retrieval with pagination and filters,
 * profile details, blocking/unblocking, deletion, bulk lookup and email validation.
 * All business logic is delegated to {@link UserService}, while this controller
 * focuses on request mapping, logging, and HTTP response handling.
 *
 * @author Romy Rose Jimmy
 * @since 2025
 */
@Slf4j
@RestController
@Tag(name = "Users", description = "API for managing users")
public class UserController {

    
    @Autowired
    private UserService usersManagementService;
    @Autowired
    private UserRepository userRepository;

    /**
     * Registers a new user in the system.
     *
     * @param reg registration request containing user details
     * @return a UsersDTO containing status and created user information
     */
    @Operation(summary = "Registration")
    @PostMapping("/register")
    public ResponseEntity<UsersDTO> register(@Valid @RequestBody UsersRequest reg) {
        log.info("Registration initiated | Email={}", reg.getEmail());
        UsersDTO response = usersManagementService.register(reg);
        log.info("Registration completed | Email={} | StatusCode={}", reg.getEmail(), response.getStatusCode());
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all users with pagination, sorting and enabled/disabled filter.
     *
     * @param pageNumber page number to retrieve
     * @param pageSize   number of users per page
     * @param sortBy     field to sort by
     * @param sortOrder  sort direction (asc/desc)
     * @param enabled    filter by enabled/disabled status
     * @return paginated list of users wrapped in UsersResponse
     */
    @Operation(summary = "Get all users")
    @GetMapping("/get-all-users")
    public ResponseEntity<UsersResponse> getAllUsers(@RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
                                                     @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
                                                     @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_USERS_BY) String sortBy,
                                                     @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR) String sortOrder,
                                                     @RequestParam(name = "enabled", defaultValue = AppConstants.STATUS) boolean enabled) {
        log.debug("Fetching users | Page={} | Size={} | SortBy={} | Order={} | Enabled={}",
                pageNumber, pageSize, sortBy, sortOrder, enabled);

        UsersResponse response = usersManagementService.getAllUsers(pageNumber, pageSize, sortBy, sortOrder, enabled);
        log.info("Users retrieved successfully | Count={} | Page={}",
                response != null ? response.getContent().size() : 0, pageNumber);

        return new ResponseEntity<>(response, HttpStatus.FOUND);
    }

    /**
     * Retrieves users matching the given keyword with pagination and sorting.
     *
     * @param keyword    text to search in user fields
     * @param pageNumber page number to retrieve
     * @param pageSize   number of users per page
     * @param sortBy     field to sort by
     * @param sortOrder  sort direction (asc/desc)
     * @return paginated list of filtered users wrapped in UsersResponse
     */
    @Operation(summary = "Get all users with filtering")
    @GetMapping("/get-all-users/keyword/{keyword}")
    public ResponseEntity<UsersResponse> getUsersByKeyword(@PathVariable String keyword,
                                                           @RequestParam(name = "pageNumber", defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
                                                           @RequestParam(name = "pageSize", defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
                                                           @RequestParam(name = "sortBy", defaultValue = AppConstants.SORT_USERS_BY) String sortBy,
                                                           @RequestParam(name = "sortOrder", defaultValue = AppConstants.SORT_DIR) String sortOrder) {
        log.debug("Searching users | Keyword={} | Page={} | Size={}", keyword, pageNumber, pageSize);

        UsersResponse response = usersManagementService.searchUsersByKeyword(keyword, pageNumber, pageSize, sortBy, sortOrder);
        log.info("Search completed | Keyword={} | Results={}", keyword,
                response != null ? response.getContent().size() : 0);

        return new ResponseEntity<>(response, HttpStatus.FOUND);
    }

    /**
     * Fetches user details by user ID.
     *
     * @param userId ID of the user to fetch
     * @return user details wrapped in UsersDTO
     */
    @Operation(summary = "Get user by ID")
    @GetMapping("/get-users/{userId}")
    public ResponseEntity<UsersDTO> getUserByID(@PathVariable UUID userId) {
        log.debug("Fetching user details | UserId={}", userId);
        UsersDTO response = usersManagementService.getUsersById(userId);

        if (response.getStatusCode() == 500) {
            log.error("Failed to fetch user | UserId={} | StatusCode=500", userId);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        log.info("User details retrieved successfully | UserId={}", userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Fetches a user by role.
     *
     * @param role role of user(s) to fetch
     * @return UsersDTO containing users matching the provided role
     */
    @Operation(summary = "Get user by role")
    @GetMapping("/get-users/find/{role}")
    public ResponseEntity<UsersDTO> getUserByRole(@PathVariable Role role) {
        log.debug("Fetching user by role | Role={}", role);
        UsersDTO response = usersManagementService.getUsersByRole(role);

        if (response.getStatusCode() == 500) {
            log.error("Failed to fetch user by role | Role={} | StatusCode=500", role);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        log.info("User fetched successfully by role | Role={}", role);
        return ResponseEntity.ok(response);
    }

    /**
     * Updates user details based on userId.
     *
     * @param userId ID of the user to update
     * @param req    updated user entity details
     * @return updated user wrapped in UsersDTO
     */
    @Operation(summary = "Update user details")
    @PutMapping("/update/{userId}")
    public ResponseEntity<UsersDTO> updateUser(@PathVariable UUID userId, @RequestBody User req) {
        log.info("User update initiated | UserId={} | Email={}", userId, req.getEmail());
        UsersDTO updatedUser = usersManagementService.updateUser(userId, req);
        log.info("User update completed | UserId={} | StatusCode={}", userId, updatedUser.getStatusCode());
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Retrieves profile information of the logged-in user using email.
     *
     * @param email email of the user
     * @return user profile wrapped in UserDTO
     */
    @Operation(summary = "Get logged in user details")
    @GetMapping("/get-profile/{email}")
    public ResponseEntity<UserDTO> getMyProfile(@PathVariable String email) {
        log.debug("Fetching profile | Email={}", email);
        UserDTO userDTO = usersManagementService.getMyInfo(email);
        log.info("Profile fetched successfully | Email={}", email);
        return ResponseEntity.ok(userDTO);
    }

    /**
     * Deletes a user by userId.
     *
     * @param userId ID of the user to delete
     * @return deletion result wrapped in UsersDTO
     */
    @Operation(summary = "Delete user")
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<UsersDTO> deleteUser(@PathVariable UUID userId) {
        log.warn("User deletion requested | UserId={}", userId);
        UsersDTO response = usersManagementService.deleteUser(userId);
        log.info("User deleted successfully | UserId={}", userId);
        return ResponseEntity.ok(response);
    }

    /**
     * Blocks a user with an optional block reason.
     *
     * @param userId ID of the user to block
     * @param req    request containing block details
     * @return result of block operation wrapped in UsersDTO
     */
    @Operation(summary = "Block user")
    @PutMapping("/block/{userId}")
    public ResponseEntity<UsersDTO> blockUser(@PathVariable UUID userId, @RequestBody UsersDTO req) {
        log.warn("User block initiated | UserId={}", userId);
        UsersDTO response = usersManagementService.blockUser(userId, req);

        if (response.getStatusCode() == 500) {
            log.error("User block failed | UserId={} | StatusCode=500", userId);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        log.info("User block action completed | UserId={} | Blocked={}", userId,
               response.getBlockReason());
        return ResponseEntity.ok(response);
    }

    /**
     * Gets block status of a user by email.
     *
     * @param email email of the user
     * @return block status wrapped in BlockStatusResponse
     */
    @Operation(summary = "Get block status")
    @GetMapping("/auth/{email}/block-status")
    public ResponseEntity<BlockStatusResponse> getBlockStatus(@PathVariable String email) {
        log.debug("Checking block status | Email={}", email);
        BlockStatusResponse response = usersManagementService.checkBlockStatus(email);
        log.info("Block status retrieved | Email={} | Blocked={}", email, response.isBlocked());
        return ResponseEntity.ok(response);
    }

    /**
     * Fetches users by a list of user IDs.
     *
     * @param request wrapper object containing list of user IDs
     * @return list of user details wrapped in FollowUserDTO
     */
    @Operation(
            summary = "Get multiple users by IDs",
            description = "Returns basic user information for a list of user IDs."
    )
    @PostMapping("/multiple")
    public ResponseEntity<List<FollowUserDTO>> getUsersByIds(@RequestBody(required = false) UserIdsRequest request) {
        if (request == null || request.getIds() == null || request.getIds().isEmpty()) {
            log.warn("Invalid request received for /multiple | Reason=Empty or null user IDs");
            return ResponseEntity.badRequest().build();
        }

        log.debug("Fetching multiple users | Count={}", request.getIds().size());

        List<FollowUserDTO> users = usersManagementService.getUsersByIds(request);
        log.info("Fetched multiple users successfully | Retrieved={}",
                users != null ? users.size() : 0);

        return ResponseEntity.ok(users);
    }


    /**
     * Validates whether an email is already registered or available.
     *
     * @param email email address to validate
     * @return map containing 'valid' and 'message' fields
     */
    @Operation(
            summary = "Validate email",
            description = "Checks whether an email is already registered or available for new registration."
    )
    @GetMapping("/validate-email")
    public ResponseEntity<Map<String, Object>> validateEmail(@RequestParam String email) {
        log.debug("Validating email availability | Email={}", email);
        boolean exists = userRepository.existsByEmailIgnoreCase(email);
        log.info("Email validation result | Email={} | Exists={}", email, exists);

        return ResponseEntity.ok(
                Map.of(
                        "valid", !exists,
                        "message", exists ? "Email already registered" : "Email is available"
                )
        );
    }

    @GetMapping("/username/{username}")
    public ResponseEntity<UserProfileResponse> getUserByUsername(
            @PathVariable String username,
            Principal principal
    ) {
        UUID loggedInUserId = principal != null
                ? UUID.fromString(principal.getName())
                : null;

        return ResponseEntity.ok(
                usersManagementService.getUserByUsername(username, loggedInUserId)
        );
    }
}
