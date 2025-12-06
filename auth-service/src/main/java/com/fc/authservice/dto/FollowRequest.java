package com.fc.authservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.UUID;

/**
 * Request DTO used to create or remove a follow relationship between users.
 * This object carries the IDs of both the follower and the user being followed.
 * It is used in follow/unfollow operations within the FollowController.
 *
 * @author Romy Rose Jimmy
 * @since 2025
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class FollowRequest {

    /** ID of the user who initiates the follow action */
    private UUID followerId;

    /** ID of the user who is being followed */
    private UUID followingId;
}
