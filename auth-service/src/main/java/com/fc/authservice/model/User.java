package com.fc.authservice.model;


import com.fc.authservice.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name="users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String userName;

    @Size(min = 10, max = 10, message = "Mobile Number must be exactly 10 digits long")
    @Pattern(regexp = "^\\d{10}$", message = "Mobile Number must contain only Numbers")
    private String mobile_number;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String image;

    private String description;

    private String district;

    private String block_reason;

    private LocalDateTime createdAt;

    private boolean enabled;

    private String otp;

    private LocalDateTime otpGeneratedTime;

}
