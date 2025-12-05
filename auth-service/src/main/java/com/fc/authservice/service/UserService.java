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
import static com.fc.authservice.config.AppConstants.USER_NOT_FOUND;
import static com.fc.authservice.config.AppConstants.USER_NOT_FOUND_EMAIL;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserService {

    // Logger for this class
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);


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

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public UsersDTO login(UsersDTO loginRequest){
        UsersDTO response = new UsersDTO();
        var user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND));
        response.setBlockReason(user.getBlockReason());
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                throw new PasswordMismatchException("Incorrect password");
            }
            if (loginRequest.getEmail() == null || loginRequest.getPassword() == null) {
                throw new BadRequestException("Username or password must not be null");
            }

            if (!user.isEnabled()) {
                throw new ForbiddenException("User account is disabled");
            }

            Optional<String> tokenOptional = authService.authenticate(loginRequest);
            String token;
            token = tokenOptional.orElse(null);

            if (tokenOptional.isEmpty()) {
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

        return response;
    }

    public UsersDTO socialLogin(SocialLoginRequest loginRequest) {
        // 1️⃣ Basic validation
        if (loginRequest.getEmail() == null || loginRequest.getEmail().isBlank()) {
            throw new BadRequestException("Email must not be null for social login");
        }

        // 2️⃣ Find existing user or create new one
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseGet(() -> createSocialUser(loginRequest));

        if (!user.isEnabled()) {
            throw new ForbiddenException("User account is disabled");
        }

        // 3️⃣ Optionally update name/avatar/provider IDs if changed
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
            userRepository.save(user);
        }

        // 4️⃣ Generate JWT for this user (no password in social login)
        Optional<String> tokenOptional = authService.generateTokenForUser(user);
        String token = tokenOptional.orElse(null);

        UsersDTO response = new UsersDTO();
        response.setToken(token);

        if (tokenOptional.isEmpty()) {
            // optional: set message or status
            return response;
        }

        // 5️⃣ Build UsersDTO exactly like your normal login()
        response.setStatusCode(200);
        response.setRole(user.getRole());
        response.setUserId(user.getId());
        response.setName(user.getUserName());
        response.setEmail(user.getEmail());
        response.setEnabled(user.isEnabled());
        response.setExpirationTime("24Hrs");
        response.setMessage("Successfully Logged In via " + loginRequest.getProvider().name());

        return response;
    }

    private User createSocialUser(SocialLoginRequest loginRequest) {
        User user = new User();
        user.setEmail(loginRequest.getEmail());
        user.setUserName(loginRequest.getName());
        user.setAvatarUrl(loginRequest.getAvatar());
        user.setEnabled(true);
        user.setSocialUser(true);
        user.setRole(Role.USER); // or your default role

        if (loginRequest.getProvider() == SocialProvider.GOOGLE) {
            user.setGoogleId(loginRequest.getProviderUserId());
        } else if (loginRequest.getProvider() == SocialProvider.FACEBOOK) {
            user.setFacebookId(loginRequest.getProviderUserId());
        }

        // set dummy password to satisfy NOT NULL constraint
        String randomPassword = UUID.randomUUID().toString();
        user.setPassword(passwordEncoder.encode(randomPassword));

        return userRepository.save(user);
    }


    public User findUserById(UUID userId) throws UserException {
        Optional<User> user=userRepository.findById(userId);

        if(user.isPresent()){
            return user.get();
        }
        throw new UserException("user not found with id "+userId);
    }

    public UsersDTO register(UsersRequest registrationRequest){
        UsersDTO resp = new UsersDTO();
        Optional<User> users = userRepository.findByEmail(registrationRequest.getEmail());

        if (users.isPresent()) {
            throw new APIException("User with the email '" + registrationRequest.getEmail() + "' already exists !!!", 409);
        }
        String otp = otpUtil.generateOtp();
        if (registrationRequest.getRole() == Role.USER && !registrationRequest.getEmail().isBlank()) {
            try {
                emailUtil.sendOtpEmail(registrationRequest.getEmail(), otp);
            } catch (MessagingException e) {
                // Wrap the internal error with a meaningful API-level message
                throw new APIException("Unable to send OTP. Please try again later.", 500);
            }
        }

        // Create new user entity
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
            }else {
                throw new APIException("Something went wrong while saving the user.", 500);
            }

        return resp;
    }


    public UsersResponse getAllUsers(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder, Boolean enabled) {

        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(Sort.Direction.ASC, sortBy)
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        Page<User> pageUsers;
        pageUsers = userRepository.findByEnabled(enabled, pageDetails);

        List<User> users = pageUsers.getContent();


        List<UserDTO> userDTOs = users.stream().map(user -> modelMapper.map(user, UserDTO.class))
                .toList();


        if (users.isEmpty()) {
            throw new APIException("No users is created till now", 404);
        }


        UsersResponse usersResponse = new UsersResponse();

        usersResponse.setMessage("Users fetched Successfully");
        usersResponse.setStatusCode(302);
        usersResponse.setContent(userDTOs);
        usersResponse.setPageNumber(pageUsers.getNumber());
        usersResponse.setPageSize(pageUsers.getSize());
        usersResponse.setTotalElements(pageUsers.getTotalElements());
        usersResponse.setTotalPages(pageUsers.getTotalPages());
        usersResponse.setLastPage(pageUsers.isLast());

        return usersResponse;
    }


    public UsersResponse searchUsersByKeyword(String keyword, Integer pageNumber, Integer pageSize, String sortBy, String sortOrder) {
        Sort sortByAndOrder = sortOrder.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageDetails = PageRequest.of(pageNumber, pageSize, sortByAndOrder);

        Page<User> pageUsers = userRepository.findByUserNameOrEmailIgnoreCaseContainingAndEnabled(keyword, keyword, true, pageDetails);

        List<User> users = pageUsers.getContent();

        if (users.isEmpty()) {
            throw new APIException("Users not found with keyword: " + keyword, 404);
        }

        List<UserDTO> usersDTOs = users.stream().map(user -> modelMapper.map(user, UserDTO.class))
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

        return usersResponse;
    }

    public UsersDTO getUsersById(UUID id) {
        UsersDTO usersDTO = new UsersDTO();
        try {
            User usersById = userRepository.findById(id).orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));

            UserDTO user = modelMapper.map(usersById, UserDTO.class);
            usersDTO.setOurUsers(user);
            usersDTO.setStatusCode(200);
            usersDTO.setMessage("Users with id '" + id + "' found successfully");
        } catch (Exception e) {
            usersDTO.setStatusCode(500);
            usersDTO.setMessage("Error occurred: " + e.getMessage());
        }
        return usersDTO;
    }

    public UsersDTO getUsersByRole(Role role) {
        UsersDTO usersDTO = new UsersDTO();
        try {
            List<User> users = userRepository.findByRoleContaining(String.valueOf(role));
            List<UserDTO> userDTO = users.stream().map(user -> modelMapper.map(user, UserDTO.class))
                    .toList();
            usersDTO.setOurUsersList(userDTO);
            usersDTO.setStatusCode(200);
            usersDTO.setMessage("Users with role '" + role + "' found successfully");
        } catch (Exception e) {
            usersDTO.setStatusCode(500);
            usersDTO.setMessage("Error occurred: " + e.getMessage());
        }
        return usersDTO;
    }

    public UsersDTO deleteUser(UUID userId) {
        UsersDTO usersDTO = new UsersDTO();
        try {
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isPresent()) {
                userRepository.deleteById(userId);
                usersDTO.setStatusCode(200);
                usersDTO.setMessage("User deleted successfully");
            } else {
                usersDTO.setStatusCode(404);
                usersDTO.setMessage("User not found for deletion");
            }
        } catch (Exception e) {
            usersDTO.setStatusCode(500);
            usersDTO.setMessage("Error occurred while deleting user: " + e.getMessage());
        }
        return usersDTO;
    }

    public UsersDTO updateUser(UUID userId, User updatedUser) {
        User existingUser = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

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
        return modelMapper.map(savedUser, UsersDTO.class);
    }


    public UserDTO getMyInfo(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        logger.debug("User description: {}", user.getDescription());

        return modelMapper.map(user, UserDTO.class);
    }



    public UsersDTO blockUser(UUID userId, UsersDTO req) {
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
            } else {
                usersDTO.setStatusCode(404);
                usersDTO.setMessage("User not found for blocking");
            }
        } catch (Exception e) {
            usersDTO.setStatusCode(500);
            usersDTO.setMessage("Error occurred while blocking user: " + e.getMessage());
        }
        return usersDTO;
    }


    public UsersDTO verifyAccount(String currentEmail, String otp, boolean isUpdateEmail, String newEmail) {
        UsersDTO usersDTO = new UsersDTO();

        // Fetch user by current email
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND_EMAIL + currentEmail));

        // Check OTP match and expiration
        if (!otp.equals(user.getOtp()) || Duration.between(user.getOtpGeneratedTime(), LocalDateTime.now()).toMinutes() > 5) {
            usersDTO.setStatusCode(500);
            usersDTO.setMessage("Invalid or expired OTP. Please regenerate and try again.");
            return usersDTO;
        }

        if (isUpdateEmail) {
            // Email update flow
            if (userRepository.findByEmail(newEmail).isPresent()) {
                usersDTO.setStatusCode(409); // Conflict
                usersDTO.setMessage("Email already in use by another user.");
                return usersDTO;
            }
            usersDTO.setStatusCode(200);
            usersDTO.setMessage("Email varified successfully.");
        } else {
            // Registration OTP verification
            user.setEnabled(true);
            userRepository.save(user);
            usersDTO.setStatusCode(200);
            usersDTO.setMessage("OTP verified. You can login.");
        }

        return usersDTO;
    }


    public UsersDTO regenerateOtp(String email, boolean isUpdateEmail, String currentEmail) {
        UsersDTO usersDTO = new UsersDTO();

        if (isUpdateEmail) {
            // Check if new email already used
            if (userRepository.findByEmail(email).isPresent()) {
                usersDTO.setStatusCode(409); // Conflict
                usersDTO.setMessage("Email already in use by another user.");
                return usersDTO;
            }

            // Find current user by current email (must exist)
            User user = userRepository.findByEmail(currentEmail)
                    .orElseThrow(() -> new RuntimeException("Current user not found."));

            String otp = otpUtil.generateOtp();

            try {
                emailUtil.sendOtpEmail(email, otp); // Send to NEW email
            } catch (MessagingException e) {
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
            // Normal verification flow
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND_EMAIL + email));

            String otp = otpUtil.generateOtp();

            try {
                emailUtil.sendOtpEmail(email, otp);
            } catch (MessagingException e) {
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


    public UsersDTO forgotPassword(String email) {
        UsersDTO usersDTO = new UsersDTO();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(USER_NOT_FOUND_EMAIL + email));

        try {
            emailUtil.sendSetPasswordEmail(user.getEmail());
        } catch (MessagingException e) {
            throw new EmailSendException("Unable to send set password email, please try again", e);
        }

        usersDTO.setStatusCode(200);
        usersDTO.setMessage("Please check your email to set new password to your account");
        return usersDTO;
    }

    public UsersDTO setPassword( String email, String newPassword){
        UsersDTO resp = new UsersDTO();
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND_EMAIL + email));

            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);

            resp.setMessage("Password reset Successfully");
            resp.setStatusCode(200);
        } catch (NoSuchElementException e){
            resp.setStatusCode(404);
            resp.setError(e.getMessage());
        } catch (Exception e){
            resp.setStatusCode(500);
            resp.setError(e.getMessage());
        }
        return resp;
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUserName((username).describeConstable().orElseThrow(() -> new UsernameNotFoundException(USER_NOT_FOUND)));
    }

    public BlockStatusResponse checkBlockStatus(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new APIException(USER_NOT_FOUND, 404));

        boolean active = user.isEnabled();
        String message = active ? "User is active" : "User is blocked";

        return new BlockStatusResponse(active, message);
    }


    public List<FollowUserDTO> getUsersByIds(UserIdsRequest request) {
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
