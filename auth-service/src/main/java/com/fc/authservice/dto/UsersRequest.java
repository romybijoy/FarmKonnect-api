package com.fc.authservice.dto;

import com.fc.authservice.annotation.ValidPassword;
import com.fc.authservice.enums.Role;
import com.fc.authservice.model.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsersRequest {

    private String name;

    private String city;

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    private String email;

    @NotBlank(message = "Mobile number is required")
    @Size(min = 10, max = 10, message = "Mobile Number must be exactly 10 digits long")
    @Pattern(regexp = "^\\d{10}$", message = "Mobile Number must contain only Numbers")
    private String mobile_number;

    @NotBlank(message = "Password is required")
    @ValidPassword
    private String password;

    private String description;
    private String district;
    private Role role;
    private String image;
    private String block_reason;
    private boolean enabled;
    private User ourUsers;

    private List<User> ourUsersList;
}