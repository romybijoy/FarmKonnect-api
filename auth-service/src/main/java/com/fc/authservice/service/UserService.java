package com.fc.authservice.service;

import com.fc.authservice.dto.*;
import com.fc.authservice.enums.Role;
import com.fc.authservice.enums.SocialProvider;
import com.fc.authservice.exception.*;
import com.fc.authservice.model.User;
import com.fc.authservice.repository.UserRepository;
import com.fc.authservice.util.EmailUtil;
import com.fc.authservice.util.JwtUtil;
import com.fc.authservice.util.OtpUtil;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static com.fc.authservice.config.AppConstants.USER_NOT_FOUND;
import static com.fc.authservice.config.AppConstants.USER_NOT_FOUND_EMAIL;

/**
 * Service layer containing core business logic for user management and authentication.
 * Responsibilities:
 * - User registration and login (normal + social)
 * - OTP generation, verification, and regeneration
 * - Password reset flow
 * - Blocking/unblocking users
 * - Fetching users with pagination, search, and filters
 * - User profile retrieval and block status check
 * - Mapping user entities to DTOs
 * Errors are surfaced via custom exceptions (UserNotFoundException, APIException, etc.)
 * and handled by the global exception handler.
 * Logging is used to trace key flows and failures for debugging and monitoring.
 *
 * @author
 *   Romy Rose Jimmy
 * @since 2025
 */
@Slf4j
@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    private OtpUtil otpUtil;

    @Autowired
    private EmailUtil emailUtil;

    @Autowired
    private JwtUtil jwtUtils;

    private final AuthService authService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    public ModelMapper modelMapper;

    public UserService(UserRepository userRepository, @Lazy AuthService authService) {
        this.userRepository = userRepository;
        this.authService = authService;
    }

    /**
     * Finds a user by email.
     *
     * @param email the email to search
     * @return Optional containing user if found
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Handles normal email/password login.
     * Validates credentials, checks account status, and generates JWT token.
     *
     * @param loginRequest login request containing email and password
     * @return UsersDTO containing token and user details if successful
     */
    public UsersDTO login(UsersDTO loginRequest) {
        log.info("Login attempt for email={}", loginRequest.getEmail());

        UsersDTO response = new UsersDTO();

        var user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> {
                    log.error("User not found for email={}", loginRequest.getEmail());
                    return new UserNotFoundException(USER_NOT_FOUND);
                });

        response.setBlockReason(user.getBlockReason());

        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            log.warn("Password mismatch for email={}", loginRequest.getEmail());
            throw new PasswordMismatchException("Incorrect password");
        }

        if (loginRequest.getEmail() == null || loginRequest.getPassword() == null) {
            log.warn("Login request contains null credentials");
            throw new BadRequestException("Username or password must not be null");
        }

        if (!user.isEnabled()) {
            log.warn("Login attempt for disabled user. email={}", loginRequest.getEmail());
            throw new ForbiddenException("User account is disabled");
        }

        Optional<String> tokenOptional = authService.authenticate(loginRequest);
        String token = tokenOptional.orElse(null);

        if (tokenOptional.isEmpty()) {
            log.error("Token generation failed for email={}", loginRequest.getEmail());
            response.setToken(token);
            return response;
        }

        response.setStatusCode(200);
        response.setToken(token);
        response.setRole(user.getRole());
        response.setUserId(user.getId());
        response.setName(user.getUserName());
        response.setEmail(user.getEmail());
        response.setEnabled(user.isEnabled());
        response.setExpirationTime("24Hrs");
        response.setMessage("Successfully Logged In");

        log.info("Login successful for email={}", loginRequest.getEmail());

        return response;
    }

    /**
     * Handles login or registration via social providers (Google/Facebook).
     *
     * @param loginRequest social login request with provider info and user details
     * @return UsersDTO containing token and user details
     */
    public UsersDTO socialLogin(SocialLoginRequest loginRequest) {
        log.info("Social login attempt for email={} using provider={}",
                loginRequest.getEmail(), loginRequest.getProvider());

        if (loginRequest.getEmail() == null || loginRequest.getEmail().isBlank()) {
            log.warn("Social login failed: email is null or blank");
            throw new BadRequestException("Email must not be null for social login");
        }

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseGet(() -> {
                    log.info("No existing user for email={} — creating new social user", loginRequest.getEmail());
                    return createSocialUser(loginRequest);
                });

        if (!user.isEnabled()) {
            log.warn("Social login failed: account disabled for email={}", loginRequest.getEmail());
            throw new ForbiddenException("User account is disabled");
        }

        boolean updated = false;

        if (loginRequest.getName() != null && !loginRequest.getName().isBlank()
                && !loginRequest.getName().equals(user.getUserName())) {
            user.setUserName(loginRequest.getName());
            updated = true;
        }

        if (loginRequest.getAvatar() != null && !loginRequest.getAvatar().isBlank()) {
            user.setAvatarUrl(loginRequest.getAvatar());
            updated = true;
        }

        if (loginRequest.getProvider() == SocialProvider.GOOGLE &&
                loginRequest.getProviderUserId() != null &&
                (user.getGoogleId() == null || !user.getGoogleId().equals(loginRequest.getProviderUserId()))) {
            user.setGoogleId(loginRequest.getProviderUserId());
            updated = true;
        }

        if (loginRequest.getProvider() == SocialProvider.FACEBOOK &&
                loginRequest.getProviderUserId() != null &&
                (user.getFacebookId() == null || !user.getFacebookId().equals(loginRequest.getProviderUserId()))) {
            user.setFacebookId(loginRequest.getProviderUserId());
            updated = true;
        }

        if (updated) {
            log.info("Updating user profile from social login for email={}", user.getEmail());
            userRepository.save(user);
        }

        Optional<String> tokenOptional = authService.generateTokenForUser(user);
        String token = tokenOptional.orElse(null);

        UsersDTO response = new UsersDTO();
        response.setToken(token);

        if (tokenOptional.isEmpty()) {
            log.error("Token generation failed for social login. email={}", user.getEmail());
            return response;
        }

        response.setStatusCode(200);
        response.setRole(user.getRole());
        response.setUserId(user.getId());
        response.setName(user.getUserName());
        response.setEmail(user.getEmail());
        response.setEnabled(user.isEnabled());
        response.setExpirationTime("24Hrs");
        response.setMessage("Successfully Logged In via " + loginRequest.getProvider().name());

        log.info("Social login successful for email={} via provider={}",
                user.getEmail(), loginRequest.getProvider());

        return response;
    }

    /**
     * Creates a new user from social login details.
     *
     * @param loginRequest social login request
     * @return newly created User entity
     */
    private User createSocialUser(SocialLoginRequest loginRequest) {
        User user = new User();
        user.setEmail(loginRequest.getEmail());
        user.setUserName(loginRequest.getName());
        user.setAvatarUrl(loginRequest.getAvatar());
        user.setEnabled(true);
        user.setSocialUser(true);
        user.setRole(Role.USER);

        if (loginRequest.getProvider() == SocialProvider.GOOGLE) {
            user.setGoogleId(loginRequest.getProviderUserId());
        } else if (loginRequest.getProvider() == SocialProvider.FACEBOOK) {
            user.setFacebookId(loginRequest.getProviderUserId());
        }

        String randomPassword = UUID.randomUUID().toString();
        user.setPassword(passwordEncoder.encode(randomPassword));

        User saved = userRepository.save(user);
        log.info("New social user created. email={} provider={}", saved.getEmail(), loginRequest.getProvider());
        return saved;
    }

    /**
     * Retrieves a user by ID or throws a UserException.
     */
    public User findUserById(UUID userId) throws UserException {
        Optional<User> user = userRepository.findById(userId);

        if (user.isPresent()) {
            return user.get();
        }
        log.error("User not found with id={}", userId);
        throw new UserException("user not found with id " + userId);
    }

    /**
     * Registers a new user with OTP-based email verification.
     *
     * @param registrationRequest incoming registration request
     * @return UsersDTO containing created user details and status
     */
    public UsersDTO register(UsersRequest registrationRequest) {
        log.info("Registration attempt for email={}", registrationRequest.getEmail());

        UsersDTO resp = new UsersDTO();
        Optional<User> users = userRepository.findByEmail(registrationRequest.getEmail());

        if (users.isPresent()) {
            log.warn("Registration failed: user already exists with email={}", registrationRequest.getEmail());
            throw new APIException("User with the email '" + registrationRequest.getEmail() + "' already exists !!!", 409);
        }

        String otp = otpUtil.generateOtp();
        if (registrationRequest.getRole() == Role.USER && !registrationRequest.getEmail().isBlank()) {
            try {
                emailUtil.sendOtpEmail(registrationRequest.getEmail(), otp);
                log.info("OTP email sent to {}", registrationRequest.getEmail());
            } catch (MessagingException e) {
                log.error("Failed to send OTP email to {}", registrationRequest.getEmail(), e);
                throw new APIException("Unable to send OTP. Please try again later.", 500);
            }
        }

        User ourUser = new User();
        ourUser.setEmail(registrationRequest.getEmail());
        ourUser.setRole(registrationRequest.getRole());
        ourUser.setUserName(registrationRequest.getName());
        ourUser.setImage(registrationRequest.getImage());
        ourUser.setMobileNumber(registrationRequest.getMobileNumber());
        ourUser.setOtp(otp);
        ourUser.setOtpGeneratedTime(LocalDateTime.now());
        ourUser.setCreatedAt(LocalDateTime.now());
        ourUser.setDescription(registrationRequest.getDescription());
        ourUser.setDistrict(registrationRequest.getDistrict());
        ourUser.setEnabled(true);
        ourUser.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));

        User savedUser = userRepository.save(ourUser);

        if (savedUser.getId() != null) {
            UserDTO user = modelMapper.map(savedUser, UserDTO.class);
            resp.setOurUsers(user);
            resp.setMessage("User Saved Successfully");
            resp.setStatusCode(200);
            log.info("User registered successfully. email={}", savedUser.getEmail());
        } else {
            log.error("User registration failed while saving to DB. email={}", registrationRequest.getEmail());
            throw new APIException("Something went wrong while saving the user.", 500);
        }

        return resp;
    }

    /**
     * Retrieves all users with pagination and enabled flag filter.
     */
    public UsersResponse getAllUsers(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, Boolean enabled) {

        log.debug("Fetching all users | page={} size={} sortBy={} sortOrder={} enabled={}",
                pageNumber, pageSize, sortBy, sortOrder, enabled);

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(Sort.Direction.ASC, sortBy)
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        Page<User> pageUsers = userRepository.findByEnabled(enabled, pageDetails);
        List<User> users = pageUsers.getContent();

        if (users.isEmpty()) {
            log.warn("No users found for enabled={}", enabled);
            throw new APIException("No users is created till now", 404);
        }

        List<UserDTO> userDTOs = users.stream()
                .map(user -> modelMapper.map(user, UserDTO.class))
                .toList();

        UsersResponse usersResponse = new UsersResponse();
        usersResponse.setMessage("Users fetched Successfully");
        usersResponse.setStatusCode(302);
        usersResponse.setContent(userDTOs);
        usersResponse.setPageNumber(pageUsers.getNumber());
        usersResponse.setPageSize(pageUsers.getSize());
        usersResponse.setTotalElements(pageUsers.getTotalElements());
        usersResponse.setTotalPages(pageUsers.getTotalPages());
        usersResponse.setLastPage(pageUsers.isLast());

        log.info("Users fetched successfully | count={} page={}", userDTOs.size(), pageNumber);

        return usersResponse;
    }

    /**
     * Searches users by keyword, matching username or email (case-insensitive).
     */
    public UsersResponse searchUsersByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        log.debug("Searching users with keyword={} | page={} size={}", keyword, pageNumber, pageSize);

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        Page<User> pageUsers =
                userRepository.findByUserNameOrEmailIgnoreCaseContainingAndEnabled(keyword, keyword, true, pageDetails);

        List<User> users = pageUsers.getContent();

        if (users.isEmpty()) {
            log.warn("No users found with keyword={}", keyword);
            throw new APIException("Users not found with keyword: " + keyword, 404);
        }

        List<UserDTO> usersDTOs = users.stream()
                .map(user -> modelMapper.map(user, UserDTO.class))
                .toList();

        UsersResponse usersResponse = new UsersResponse();
        usersResponse.setMessage("Users fetched Successfully");
        usersResponse.setStatusCode(302);
        usersResponse.setContent(usersDTOs);
        usersResponse.setPageNumber(pageUsers.getNumber());
        usersResponse.setPageSize(pageUsers.getSize());
        usersResponse.setTotalElements(pageUsers.getTotalElements());
        usersResponse.setTotalPages(pageUsers.getTotalPages());
        usersResponse.setLastPage(pageUsers.isLast());

        log.info("Search completed for keyword={} | results={}", keyword, usersDTOs.size());

        return usersResponse;
    }

    /**
     * Retrieves a single user by ID and wraps it in UsersDTO.
     */
    public UsersDTO getUsersById(UUID id) {
        log.debug("Fetching user by id={}", id);

        UsersDTO usersDTO = new UsersDTO();
        try {
            User usersById = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));

            UserDTO user = modelMapper.map(usersById, UserDTO.class);
            usersDTO.setOurUsers(user);
            usersDTO.setStatusCode(200);
            usersDTO.setMessage("Users with id '" + id + "' found successfully");
            log.info("User found by id={}", id);
        } catch (Exception e) {
            log.error("Error while fetching user by id={} | error={}", id, e.getMessage());
            usersDTO.setStatusCode(500);
            usersDTO.setMessage("Error occurred: " + e.getMessage());
        }
        return usersDTO;
    }

    /**
     * Retrieves users by role and wraps them in UsersDTO.
     */
    public UsersDTO getUsersByRole(Role role) {
        log.debug("Fetching users by role={}", role);

        UsersDTO usersDTO = new UsersDTO();
        try {
            List<User> users = userRepository.findByRoleContaining(String.valueOf(role));
            List<UserDTO> userDTO = users.stream()
                    .map(user -> modelMapper.map(user, UserDTO.class))
                    .toList();
            usersDTO.setOurUsersList(userDTO);
            usersDTO.setStatusCode(200);
            usersDTO.setMessage("Users with role '" + role + "' found successfully");
            log.info("Users fetched by role={} | count={}", role, userDTO.size());
        } catch (Exception e) {
            log.error("Error while fetching users by role={} | error={}", role, e.getMessage());
            usersDTO.setStatusCode(500);
            usersDTO.setMessage("Error occurred: " + e.getMessage());
        }
        return usersDTO;
    }

    /**
     * Deletes a user by ID.
     */
    public UsersDTO deleteUser(UUID userId) {
        log.warn("User delete requested for id={}", userId);

        UsersDTO usersDTO = new UsersDTO();
        try {
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isPresent()) {
                userRepository.deleteById(userId);
                usersDTO.setStatusCode(200);
                usersDTO.setMessage("User deleted successfully");
                log.info("User deleted successfully. id={}", userId);
            } else {
                usersDTO.setStatusCode(404);
                usersDTO.setMessage("User not found for deletion");
                log.warn("User not found for deletion. id={}", userId);
            }
        } catch (Exception e) {
            log.error("Error while deleting user id={} | error={}", userId, e.getMessage());
            usersDTO.setStatusCode(500);
            usersDTO.setMessage("Error occurred while deleting user: " + e.getMessage());
        }
        return usersDTO;
    }

    /**
     * Updates an existing user by ID using the provided User object.
     */
    public UsersDTO updateUser(UUID userId, User updatedUser) {
        log.info("User update requested for id={}", userId);

        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.error("User not found for update. id={}", userId);
                    return new UserNotFoundException("User not found with ID: " + userId);
                });

        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setUserName(updatedUser.getUserName());
        existingUser.setMobileNumber(updatedUser.getMobileNumber());
        existingUser.setImage(updatedUser.getImage());
        existingUser.setDescription(updatedUser.getDescription());
        existingUser.setDistrict(updatedUser.getDistrict());

        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        User savedUser = userRepository.save(existingUser);
        log.info("User updated successfully. id={}", userId);

        return modelMapper.map(savedUser, UsersDTO.class);
    }

    /**
     * Retrieves logged-in user profile by email.
     */
    public UserDTO getMyInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found with email={}", email);
                    return new UserNotFoundException("User not found with email: " + email);
                });

        log.debug("User profile fetched for email={}", email);
        log.debug("User description: {}", user.getDescription());

        return modelMapper.map(user, UserDTO.class);
    }

    /**
     * Blocks a user and sets the block reason.
     */
    public UsersDTO blockUser(UUID userId, UsersDTO req) {
        log.warn("Block user requested for id={}", userId);

        UsersDTO usersDTO = new UsersDTO();
        try {
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isPresent()) {
                User existingUser = userOptional.get();
                existingUser.setEnabled(false);
                existingUser.setBlockReason(req.getBlockReason());
                userRepository.updateBlockInfo(req.getBlockReason(), userId);
                usersDTO.setStatusCode(200);
                usersDTO.setMessage("User blocked successfully");
                log.info("User blocked successfully. id={}", userId);
            } else {
                usersDTO.setStatusCode(404);
                usersDTO.setMessage("User not found for blocking");
                log.warn("User not found for blocking. id={}", userId);
            }
        } catch (Exception e) {
            log.error("Error while blocking user id={} | error={}", userId, e.getMessage());
            usersDTO.setStatusCode(500);
            usersDTO.setMessage("Error occurred while blocking user: " + e.getMessage());
        }
        return usersDTO;
    }

    /**
     * Verifies the account using OTP for either normal registration or email update.
     */
    public UsersDTO verifyAccount(String currentEmail, String otp, boolean isUpdateEmail, String newEmail) {
        log.info("Account verification requested | currentEmail={} isUpdateEmail={}", currentEmail, isUpdateEmail);

        UsersDTO usersDTO = new UsersDTO();

        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> {
                    log.error("User not found for OTP verification. email={}", currentEmail);
                    return new RuntimeException(USER_NOT_FOUND_EMAIL + currentEmail);
                });

        if (!otp.equals(user.getOtp())
                || Duration.between(user.getOtpGeneratedTime(), LocalDateTime.now()).toMinutes() > 5) {

            log.warn("Invalid or expired OTP for email={}", currentEmail);
            usersDTO.setStatusCode(500);
            usersDTO.setMessage("Invalid or expired OTP. Please regenerate and try again.");
            return usersDTO;
        }

        if (isUpdateEmail) {
            if (userRepository.findByEmail(newEmail).isPresent()) {
                log.warn("Email already in use while updating. newEmail={}", newEmail);
                usersDTO.setStatusCode(409);
                usersDTO.setMessage("Email already in use by another user.");
                return usersDTO;
            }
            usersDTO.setStatusCode(200);
            usersDTO.setMessage("Email varified successfully.");
            log.info("Email verification successful for email update. currentEmail={} newEmail={}",
                    currentEmail, newEmail);
        } else {
            user.setEnabled(true);
            userRepository.save(user);
            usersDTO.setStatusCode(200);
            usersDTO.setMessage("OTP verified. You can login.");
            log.info("Account verification successful for email={}", currentEmail);
        }

        return usersDTO;
    }

    /**
     * Regenerates OTP for registration or email update flows.
     */
    public UsersDTO regenerateOtp(String email, boolean isUpdateEmail, String currentEmail) {
        log.info("Regenerate OTP requested | email={} isUpdateEmail={} currentEmail={}",
                email, isUpdateEmail, currentEmail);

        UsersDTO usersDTO = new UsersDTO();

        if (isUpdateEmail) {

            if (userRepository.findByEmail(email).isPresent()) {
                log.warn("New email already in use during OTP regenerate. newEmail={}", email);
                usersDTO.setStatusCode(409);
                usersDTO.setMessage("Email already in use by another user.");
                return usersDTO;
            }

            User user = userRepository.findByEmail(currentEmail)
                    .orElseThrow(() -> {
                        log.error("Current user not found for OTP regenerate. currentEmail={}", currentEmail);
                        return new RuntimeException("Current user not found.");
                    });

            String otp = otpUtil.generateOtp();

            try {
                emailUtil.sendOtpEmail(email, otp);
                log.info("OTP sent to new email={}", email);
            } catch (MessagingException e) {
                log.error("Failed to send OTP to new email={}", email, e);
                usersDTO.setStatusCode(500);
                usersDTO.setMessage("Unable to send OTP to new email. Please try again.");
                return usersDTO;
            }

            user.setOtp(otp);
            user.setOtpGeneratedTime(LocalDateTime.now());
            userRepository.save(user);

            usersDTO.setStatusCode(200);
            usersDTO.setMessage("OTP sent to new email. Please verify within 5 minutes.");
            return usersDTO;

        } else {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> {
                        log.error("User not found for OTP regenerate. email={}", email);
                        return new RuntimeException(USER_NOT_FOUND_EMAIL + email);
                    });

            String otp = otpUtil.generateOtp();

            try {
                emailUtil.sendOtpEmail(email, otp);
                log.info("OTP sent to email={}", email);
            } catch (MessagingException e) {
                log.error("Failed to send OTP to email={}", email, e);
                usersDTO.setStatusCode(500);
                usersDTO.setMessage("Unable to send OTP. Please try again.");
                return usersDTO;
            }

            user.setOtp(otp);
            user.setOtpGeneratedTime(LocalDateTime.now());
            userRepository.save(user);

            usersDTO.setStatusCode(200);
            usersDTO.setMessage("OTP sent to your email. Please verify within 5 minutes.");
            return usersDTO;
        }
    }

    /**
     * Initiates forgot password flow by sending a reset email.
     */
    public UsersDTO forgotPassword(String email) {
        log.info("Forgot password requested for email={}", email);

        UsersDTO usersDTO = new UsersDTO();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found for forgot password. email={}", email);
                    return new UserNotFoundException(USER_NOT_FOUND_EMAIL + email);
                });

        try {
            emailUtil.sendSetPasswordEmail(user.getEmail());
            log.info("Set password email sent to {}", user.getEmail());
        } catch (MessagingException e) {
            log.error("Failed to send set password email to {}", user.getEmail(), e);
            throw new EmailSendException("Unable to send set password email, please try again", e);
        }

        usersDTO.setStatusCode(200);
        usersDTO.setMessage("Please check your email to set new password to your account");
        return usersDTO;
    }

    /**
     * Sets a new password for the user with the given email.
     */
    public UsersDTO setPassword(String email, String newPassword) {
        log.info("Set password requested for email={}", email);

        UsersDTO resp = new UsersDTO();
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND_EMAIL + email));

            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            resp.setMessage("Password reset Successfully");
            resp.setStatusCode(200);
            log.info("Password reset successfully for email={}", email);
        } catch (NoSuchElementException e) {
            log.warn("User not found while resetting password. email={}", email);
            resp.setStatusCode(404);
            resp.setError(e.getMessage());
        } catch (Exception e) {
            log.error("Error while resetting password for email={} | error={}", email, e.getMessage());
            resp.setStatusCode(500);
            resp.setError(e.getMessage());
        }
        return resp;
    }

    /**
     * Fetches a user by username.
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUserName(username.describeConstable()
                .orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND)));
    }

    /**
     * Checks if a user is blocked or active, returning a BlockStatusResponse.
     */
    public BlockStatusResponse checkBlockStatus(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("User not found while checking block status. email={}", email);
                    return new APIException(USER_NOT_FOUND, 404);
                });

        boolean active = user.isEnabled();
        String message = active ? "User is active" : "User is blocked";

        log.info("Block status checked for email={} | active={}", email, active);

        return new BlockStatusResponse(active, message);
    }

    /**
     * Fetches multiple users by a list of IDs and maps them to FollowUserDTO.
     */
    public List<FollowUserDTO> getUsersByIds(UserIdsRequest request) {
        log.debug("Fetching users by IDs | count={}", request.getIds() != null ? request.getIds().size() : 0);

        List<User> users = userRepository.findByIdIn(request.getIds());
        return users.stream()
                .map(user -> new FollowUserDTO(
                        user.getId(),
                        user.getUserName(),
                        user.getImage(),
                        user.getEmail()
                ))
                .toList();
    }
}
