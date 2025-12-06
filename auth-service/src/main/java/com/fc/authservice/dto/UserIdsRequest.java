package com.fc.authservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Request DTO used for fetching multiple users by their IDs.
 * This is commonly used in batch operations such as:
 * - Getting user details for follower/following lists
 * - Fetching user information for group chat or tagging features
 * - Any API requiring lookup of multiple user profiles at once
 * Contains a list of UUIDs representing user IDs.
 *
 * @author Romy Rose Jimmy
 * @since 2025
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserIdsRequest {

    /** List of unique user IDs to retrieve */
    private List<UUID> ids;
}
