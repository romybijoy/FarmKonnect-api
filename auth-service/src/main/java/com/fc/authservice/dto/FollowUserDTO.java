package com.fc.authservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

/**
 * DTO representing basic user information used in follower/following lists.
 * This object is returned when retrieving users who follow another user
 * or users that a given user is following. It contains only the essential
 * public-facing user data required for social features.
 * Fields include user ID, username, profile picture URL, and email.
 *
 * @author Romy Rose Jimmy
 * @since 2025
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
@AllArgsConstructor
public class FollowUserDTO {

    /** Unique identifier of the user */
    private UUID id;

    /** Display name of the user */
    private String username;

    /** URL of the user's profile picture */
    private String profilePicture;

    /** Email address associated with the user */
    private String email;

}
