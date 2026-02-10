package com.fc.authservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * User Profile Response
 *
 * @author Romyb
 * @since 04/01/2026
 */
@Getter
@Setter
@Builder
public class UserProfileResponse {

    private UUID userId;
    private String username;
    private String name;
    private String bio;
    private String imageUrl;

    private String district;

    private int followersCount;
    private int followingCount;

    private boolean isFollowing;
}
