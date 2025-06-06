package com.fc.authservice.model;


import com.fc.authservice.annotation.ValidPassword;
import com.fc.authservice.enums.Role;
import jakarta.persistence.*;
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
    @ValidPassword
    private String password;

    @Column(name = "user_name", nullable = false)
    private String userName;

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
