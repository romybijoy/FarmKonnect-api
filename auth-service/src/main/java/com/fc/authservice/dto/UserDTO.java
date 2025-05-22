package com.fc.authservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fc.authservice.enums.Role;
import lombok.Data;

import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDTO {

    private UUID id;
    private String name;
    private String city;
    private String email;
    private String mobile_number;
    private Role role;
    private String image;
    private String block_reason;
    private Double walletBalance;
    private boolean enabled;

}
