package com.fc.postservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Lightweight DTO containing basic user profile information.
 * This DTO is commonly used in:
 *  - Post creation (e.g., repost metadata)
 *  - Comment and feed rendering
 *  - Notification display
 * Note:
 *  - This DTO intentionally contains only minimal fields
 *    to reduce payload size in microservice communication.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {

    /** Display name of the user */
    private String name;

    /** Public URL of the user's profile image */
    private String profileImage;
}
