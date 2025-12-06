package com.fc.authservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

/**
 * DTO representing the follow statistics for a user.
 * Used to return the number of followers and the number of users
 * the specified user is following.
 * This response is commonly used in social features such as
 * profile views, follower summaries, and activity dashboards.
 *
 * @author Romy Rose Jimmy
 * @since 2025
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
public class FollowCountResponse {

    /** Unique identifier of the user */
    private UUID userId;


    /** Total number of users who follow this user */
    private long followerCount;

    /** Total number of users this user is following */
    private long followingCount;
}
