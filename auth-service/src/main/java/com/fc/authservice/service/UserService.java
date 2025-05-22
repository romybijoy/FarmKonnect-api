package com.fc.authservice.service;

import com.fc.authservice.dto.UserDTO;
import com.fc.authservice.dto.UsersDTO;
import com.fc.authservice.dto.UsersRequest;
import com.fc.authservice.dto.UsersResponse;
import com.fc.authservice.enums.Role;
import com.fc.authservice.exception.APIException;
import com.fc.authservice.exception.UserException;
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
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

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
//
//    @Autowired
//    private AuthenticationManager authenticationManager;

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
        var user = userRepository.findByEmail(loginRequest.getEmail()).orElseThrow();
        response.setBlock_reason(user.getBlock_reason());
        try {

//            authenticationManager
//                    .authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getEmail(),
//                            loginRequest.getPassword()));
//            var jwt = jwtUtils.generateToken(user);
//            var refreshToken = jwtUtils.generateRefreshToken(new HashMap<>(), user);
            Optional<String> tokenOptional = authService.authenticate(loginRequest);
            String token;
            token = tokenOptional.orElse(null);

            response.setStatusCode(200);
            response.setToken(token);
            response.setRole(user.getRole());
            response.setName(user.getUserName());
            response.setEmail(user.getEmail());
//            response.setRefreshToken(refreshToken);
            response.setExpirationTime("24Hrs");
            response.setMessage("Successfully Logged In");

        }catch (DisabledException e) {

            response.setMessage("User is disabled due to "+ user.getBlock_reason());
            response.setStatusCode(403);
        }
        catch (Exception e){
            response.setStatusCode(500);
            response.setMessage(e.getMessage());
        }
        return response;
    }





//    public UsersDTO refreshToken(UsersDTO refreshTokenRequest){
//        UsersDTO response = new UsersDTO();
//        try{
//            String ourEmail = jwtUtils.extractUsername(refreshTokenRequest.getToken());
//            OurUsers users = usersRepo.findByEmail(ourEmail).orElseThrow();
//            if (jwtUtils.isTokenValid(refreshTokenRequest.getToken(), users)) {
//                var jwt = jwtUtils.generateToken(users);
//                response.setStatusCode(200);
//                response.setToken(jwt);
//                response.setRefreshToken(refreshTokenRequest.getToken());
//                response.setExpirationTime("24Hr");
//                response.setMessage("Successfully Refreshed Token");
//            }
//            response.setStatusCode(200);
//            return response;
//
//        }catch (Exception e){
//            response.setStatusCode(500);
//            response.setMessage(e.getMessage());
//            return response;
//        }
//    }



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
        if(registrationRequest.getRole() == Role.USER){

            if(!Objects.equals(registrationRequest.getEmail(), "")) {

                try {
                    emailUtil.sendOtpEmail(registrationRequest.getEmail(), otp);
                } catch (MessagingException e) {
                    throw new RuntimeException("Unable to send otp please try again");
                }
            }}
        try {
            User ourUser = new User();
            ourUser.setEmail(registrationRequest.getEmail());
            ourUser.setRole(registrationRequest.getRole());
            ourUser.setUserName(registrationRequest.getName());
            ourUser.setImage(registrationRequest.getImage());
            ourUser.setMobile_number(registrationRequest.getMobile_number());
            ourUser.setOtp(otp);
            ourUser.setOtpGeneratedTime(LocalDateTime.now());
            ourUser.setEnabled(true);
            ourUser.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
            User userResult = userRepository.save(ourUser);

            if (userResult.getId() != null) {

                UserDTO user = modelMapper.map(userResult, UserDTO.class);
                resp.setOurUsers(user);
                resp.setMessage("User Saved Successfully");
                resp.setStatusCode(200);
            }

        }catch (Exception e){
            resp.setStatusCode(500);
            resp.setError(e.getMessage());
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
            User usersById = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User Not found"));

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
        UsersDTO usersDTO = new UsersDTO();
        try {
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isPresent()) {
                User existingUser = userOptional.get();
                existingUser.setEmail(updatedUser.getEmail());
                existingUser.setUserName(updatedUser.getUserName());
                existingUser.setMobile_number(updatedUser.getMobile_number());
                existingUser.setRole(updatedUser.getRole());
                existingUser.setImage(updatedUser.getImage());

                // Check if password is present in the request
                if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
                    // Encode the password and update it
                    existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
                }

                User savedUser = userRepository.save(existingUser);
                UserDTO user = modelMapper.map(savedUser, UserDTO.class);
                usersDTO.setOurUsers(user);
                usersDTO.setStatusCode(200);
                usersDTO.setMessage("User updated successfully");
            } else {
                usersDTO.setStatusCode(404);
                usersDTO.setMessage("User not found for update");
            }
        } catch (Exception e) {
            usersDTO.setStatusCode(500);
            usersDTO.setMessage("Error occurred while updating user: " + e.getMessage());
        }
        return usersDTO;
    }


    public UsersDTO getMyInfo(String email){
        UsersDTO usersDTO = new UsersDTO();
        try {
            Optional<User> userOptional = userRepository.findByEmail(email);
            if (userOptional.isPresent()) {

                UserDTO user = modelMapper.map(userOptional.get(), UserDTO.class);
                usersDTO.setOurUsers(user);
                usersDTO.setStatusCode(200);
                usersDTO.setMessage("successful");
            } else {
                usersDTO.setStatusCode(404);
                usersDTO.setMessage("User not found");
            }

        }catch (Exception e){
            usersDTO.setStatusCode(500);
            usersDTO.setMessage("Error occurred while getting user info: " + e.getMessage());
        }
        return usersDTO;

    }

    public UsersDTO blockUser(UUID userId, UsersDTO req) {
        UsersDTO usersDTO = new UsersDTO();
        try {
            Optional<User> userOptional = userRepository.findById(userId);
            if (userOptional.isPresent()) {
                User existingUser = userOptional.get();
                existingUser.setEnabled(false);
                existingUser.setBlock_reason(req.getBlock_reason());
                userRepository.updateBlockInfo(req.getBlock_reason(), userId);
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


    public UsersDTO verifyAccount(String email, String otp) {
        UsersDTO usersDTO = new UsersDTO();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with this email: " + email));
        if (user.getOtp().equals(otp) && Duration.between(user.getOtpGeneratedTime(),
                LocalDateTime.now()).getSeconds() < (60 * 60)) {
            user.setEnabled(true);
            userRepository.save(user);
            usersDTO.setStatusCode(200);
            usersDTO.setMessage("OTP verified you can login");
            return usersDTO;
        }
        usersDTO.setStatusCode(500);
        usersDTO.setMessage("Please regenerate otp and try again");
        return usersDTO;
    }

    public UsersDTO regenerateOtp(String email) {
        UsersDTO usersDTO = new UsersDTO();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with this email: " + email));
        String otp = otpUtil.generateOtp();
        try {
            emailUtil.sendOtpEmail(email, otp);
        } catch (MessagingException e) {
            throw new RuntimeException("Unable to send otp please try again");
        }
        user.setOtp(otp);
        user.setOtpGeneratedTime(LocalDateTime.now());
        userRepository.save(user);
        usersDTO.setStatusCode(200);
        usersDTO.setMessage("Email sent... please verify account within 1 minute");
        return usersDTO;
    }

    public UsersDTO forgotPassword(String email) {
        UsersDTO usersDTO = new UsersDTO();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with this email: " + email));
        try {
            emailUtil.sendSetPasswordEmail(email);
        } catch (MessagingException e) {
            throw new RuntimeException("Unable to send set password email please try again");
        }

        usersDTO.setStatusCode(200);
        usersDTO.setMessage("Please check your email to set new password to your account");
        return usersDTO;
    }

    public UsersDTO setPassword( String email, String newPassword){
        UsersDTO resp = new UsersDTO();
        try {
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found with this email: " + email));

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
}
