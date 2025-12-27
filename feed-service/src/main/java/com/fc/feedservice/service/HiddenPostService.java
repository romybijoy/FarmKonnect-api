package com.fc.feedservice.service;

import com.fc.feedservice.model.HiddenPost;
import com.fc.feedservice.repository.HiddenPostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Service that manages posts hidden by a user.
 * Provides methods to:
 * - Hide a post from a user's feed
 * - Retrieve all hidden post IDs for a user
 */
@Slf4j
@Service
public class HiddenPostService {
    private final HiddenPostRepository hiddenPostRepository;


    public HiddenPostService(HiddenPostRepository hiddenPostRepository) {
        this.hiddenPostRepository = hiddenPostRepository;
    }

    /**
     * Hides a post from a user's feed.
     * If the post is already hidden, nothing is changed.
     *
     * @param userId ID of the user performing the hide action
     * @param postId ID of the post to be hidden
     */
    public void hidePost(UUID userId, UUID postId) {
        if (!hiddenPostRepository.existsByUserIdAndPostId(userId, postId)) {
            log.debug("Hiding post for user. userId={}, postId={}", userId, postId);
            HiddenPost hiddenPost = new HiddenPost();
            hiddenPost.setUserId(userId);
            hiddenPost.setPostId(postId);
            hiddenPostRepository.save(hiddenPost);
        }else{
            log.debug("Post already hidden. userId={}, postId={}", userId, postId);
        }
    }

    /**
     * Fetches all hidden post IDs for a given user.
     *
     * @param userId ID of the user
     * @return list of postId values that the user has hidden
     */
    public List<UUID> getHiddenPostsForUser(UUID userId) {
        log.debug("Fetching hidden posts for userId={}", userId);

        return hiddenPostRepository.findByUserId(userId)
                .stream()
                .map(HiddenPost::getPostId)
                .toList();
    }
}
