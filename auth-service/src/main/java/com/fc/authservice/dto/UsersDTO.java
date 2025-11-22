package com.fc.authservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fc.authservice.enums.Role;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UsersDTO {

    private int statusCode;
    private String error;
    private String token;
    private UUID userId;
    private String message;
    private String refreshToken;
    private String expirationTime;
    private String name;
    private String city;
    private String email;
    private String mobileNumber;
    private String password;
    private Role role;
    private String image;
    private String blockReason;
    private boolean enabled;
    private UserDTO ourUsers;
    private List<UserDTO> ourUsersList;

}
