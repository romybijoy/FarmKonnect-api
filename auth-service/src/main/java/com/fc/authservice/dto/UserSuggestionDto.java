package com.fc.authservice.dto;

import java.util.UUID;

/**
 * DTO representing a user suggestion item returned to the client.
 * Contains basic profile information along with ranking metadata such as
 * district and mutual follower count, which helps determine the user's
 * relevance in the suggestion list.
 *
 * @author Romy
 * @since 10/12/2025
 */
public record UserSuggestionDto(
        UUID id,
        String userName,
        String image,
        String district,
        long mutualFollowersCount
) {}

