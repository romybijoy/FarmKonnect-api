package com.fc.authservice.dto;

import com.fc.authservice.enums.Role;
import com.fc.authservice.model.User;
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
    private String email;
    private String mobile_number;
    private String password;
    private Role role;
    private String image;
    private String block_reason;
    private boolean enabled;
    private User ourUsers;

    private List<User> ourUsersList;
}