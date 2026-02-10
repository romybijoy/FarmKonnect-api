package com.fc.authservice.service;

import com.fc.authservice.dto.UserSuggestionDto;
import com.fc.authservice.model.User;
import com.fc.authservice.repository.FollowRepository;
import com.fc.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * SuggestionService encapsulates the business logic for computing
 * follow recommendations for a user.
 * It fetches candidate users based on district proximity, ranks them
 * by mutual follower count, and returns a refined list of suggestions.
 * Core responsibilities:
 * - Exclude the requesting user.
 * - Exclude users already followed by the requester.
 * - Prioritize users from the same district.
 * - Compute the number of mutual followers for ranking relevance.
 * This service provides the backend intelligence behind the "Suggestions for you" feature.
 *
 * @author Romy Rose Jimmy
 * @since 10/12/2025
 */
@Service
@RequiredArgsConstructor
public class SuggestionService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    public List<UserSuggestionDto> getSuggestions(UUID meId, int size) {
        User me = userRepository.findById(meId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String district = me.getDistrict();
        if (district == null || district.isBlank()) {
            district = ""; // fallback, still works with query
        }

        // Fetch more candidates than needed, then sort by mutuals in memory
        Page<User> candidates = userRepository.findSuggestionCandidates(
                meId,
                district,
                PageRequest.of(0, size * 2)    // fetch a bit more to rank by mutuals
        );

        List<UserSuggestionDto> result = candidates.getContent().stream()
                .map(user -> {
                    long mutualCount = followRepository.countMutualFollowSources(meId, user.getId());

                    return new UserSuggestionDto(
                            user.getId(),
                            user.getUserName(),
                            user.getImage(),
                            user.getDistrict(),
                            mutualCount
                    );
                })
                // sort: first by mutual followers desc, then by district priority already handled by SQL ordering
                .sorted(Comparator.comparingLong(UserSuggestionDto::mutualFollowersCount).reversed())
                .limit(size)
                .toList();

        return result;
    }

}
